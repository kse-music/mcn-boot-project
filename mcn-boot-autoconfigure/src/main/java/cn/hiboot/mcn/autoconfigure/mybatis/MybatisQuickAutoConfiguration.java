package cn.hiboot.mcn.autoconfigure.mybatis;

import cn.hiboot.mcn.core.config.McnConstant;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

/**
 * MybatisQuickAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/2 0:25
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SqlSessionFactory.class, MapperScan.class})
@ConditionalOnMissingBean(MapperFactoryBean.class)
public class MybatisQuickAutoConfiguration {

    private static final String MULTIPLY_DATASOURCE_CONFIG_KEY = "multiply.datasource.name";

    @Configuration(proxyBeanMethods = false)
    private static class MapperScannerBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ResourceLoaderAware,Ordered {

        private ResourceLoader resourceLoader;
        private Environment environment;

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            String[] dbs = environment.getProperty(MULTIPLY_DATASOURCE_CONFIG_KEY, String[].class);
            String basePackage = environment.getProperty(McnConstant.APP_BASE_PACKAGE);

            if(dbs == null || dbs.length == 0){
                configDefaultScanPackage(basePackage,registry);
            }else {
                configMultipleDataSource(basePackage,dbs,registry);
            }
        }

        private void configDefaultScanPackage(String basePackage,BeanDefinitionRegistry registry){
           try{
               BeanDefinition beanDefinition = registry.getBeanDefinition(MapperScannerConfigurer.class.getName());
               beanDefinition.getPropertyValues().removePropertyValue("annotationClass");
               String toScanPkg = basePackage + ".dao";
               String pkg = environment.getProperty("mapper.scan.additional-package", "");
               if(StringUtils.hasText(pkg)){
                   toScanPkg += "," + pkg;
               }
               beanDefinition.getPropertyValues().addPropertyValue("basePackage", toScanPkg);
           }catch (Exception e){
               //ignore default MapperScannerConfigurer bdf not exist
           }
       }

        private void configMultipleDataSource(String basePackage,String[] dbs,BeanDefinitionRegistry registry){
            for (String dsName : dbs) {
                String sqlSessionFactoryName = dsName + "SqlSessionFactory";

                ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
                if (resourceLoader != null) {
                    scanner.setResourceLoader(resourceLoader);
                }
                scanner.setSqlSessionFactoryBeanName(sqlSessionFactoryName);
                scanner.registerFilters();
                scanner.doScan(basePackage + ".dao." + dsName);

                String dataSourceName = dsName + "DataSource";
                registry.registerBeanDefinition(dataSourceName,BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class)
                        .setRole(BeanDefinition.ROLE_INFRASTRUCTURE).setSynthetic(true).getBeanDefinition());

                SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
                factoryBean.setVfs(SpringBootVFS.class);
                ResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
                org.apache.ibatis.session.Configuration conf = new org.apache.ibatis.session.Configuration();
                conf.setMapUnderscoreToCamelCase(true);
                try {
                    registry.registerBeanDefinition(sqlSessionFactoryName,
                            BeanDefinitionBuilder.rootBeanDefinition(SqlSessionFactoryBean.class).setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                                    .addPropertyValue("vfs", SpringBootVFS.class)
                                    .addPropertyValue("dataSource", new RuntimeBeanReference(dataSourceName))
                                    .addPropertyValue("mapperLocations", pathResolver.getResources("classpath*:mapper/"+dsName+"/*.xml"))
                                    .addPropertyValue("typeAliasesPackage", basePackage + ".bean")
                                    .addPropertyValue("typeHandlersPackage", basePackage + ".dao.handler")
                                    .addPropertyValue("configuration", conf)
                                    .getBeanDefinition());
                } catch (Exception e) {
                    //
                }
            }
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }
    }

}
