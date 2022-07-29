package cn.hiboot.mcn.autoconfigure.mybatis;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.jdbc.MultipleDataSourceAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.jdbc.MultipleDataSourceConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;

import java.io.IOException;

/**
 * MultipleDataSourceAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/2 22:21
 */
@AutoConfiguration(after = MultipleDataSourceAutoConfiguration.class)
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class,HikariDataSource.class})
@ConditionalOnProperty(prefix = ConfigProperties.MYBATIS_MULTIPLE_DATASOURCE_PREFIX,name = "enable",havingValue = "true")
@ConditionalOnBean(MultipleDataSourceConfig.class)
@Import(MybatisMultipleDataSourceAutoConfiguration.MybatisMultipleDataSourceConfig.class)
public class MybatisMultipleDataSourceAutoConfiguration {

    protected static class MybatisMultipleDataSourceConfig implements ImportBeanDefinitionRegistrar {
        private final ResourceLoader resourceLoader;
        private final Environment environment;
        private final MultipleDataSourceConfig multipleDataSourceConfig;

        public MybatisMultipleDataSourceConfig(ResourceLoader resourceLoader, Environment environment, BeanFactory beanFactory) {
            this.resourceLoader = resourceLoader;
            this.environment = environment;
            this.multipleDataSourceConfig = beanFactory.getBean(MultipleDataSourceConfig.class);
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry){
            String basePackage = environment.getProperty(ConfigProperties.APP_BASE_PACKAGE);
            ResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
            multipleDataSourceConfig.getProperties().forEach((dsName,ds) -> {
                String sqlSessionFactoryName = dsName + "SqlSessionFactory";
                scanMapper(registry,sqlSessionFactoryName,basePackage + ".dao." + dsName);

                registry.registerBeanDefinition(dsName + "sqlSessionTemplate", BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class)
                        .addConstructorArgReference(sqlSessionFactoryName)
                        .getBeanDefinition());

                SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
                factoryBean.setVfs(SpringBootVFS.class);
                org.apache.ibatis.session.Configuration conf = new org.apache.ibatis.session.Configuration();
                conf.setMapUnderscoreToCamelCase(true);
                registry.registerBeanDefinition(sqlSessionFactoryName,
                        loadMapper(pathResolver, dsName)
                                .addPropertyValue("dataSource", new RuntimeBeanReference(ConfigProperties.getDataSourceBeanName(dsName)))
                                .addPropertyValue("typeAliasesPackage", basePackage + ".bean" + dsName)
                                .addPropertyValue("typeHandlersPackage", basePackage + ".dao.handler" + dsName)
                                .addPropertyValue("configuration", conf).getBeanDefinition());

            });
        }

        private BeanDefinitionBuilder loadMapper(ResourcePatternResolver pathResolver, String dsName){
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactoryBean.class).addPropertyValue("vfs", SpringBootVFS.class);
            Resource[] resources = null;
            try {
                resources = pathResolver.getResources("classpath:mapper/" + dsName + "/*.xml");
            } catch (IOException e) {
                //ignore not exist
            }
            if(resources != null){
                beanDefinitionBuilder.addPropertyValue("mapperLocations", resources);
            }
            return beanDefinitionBuilder;
        }

        private void scanMapper(BeanDefinitionRegistry registry, String sqlSessionFactoryName, String pkg){
            ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
            if (resourceLoader != null) {
                scanner.setResourceLoader(resourceLoader);
            }
            scanner.setBeanNameGenerator(new FullyQualifiedAnnotationBeanNameGenerator());
            scanner.setSqlSessionFactoryBeanName(sqlSessionFactoryName);
            scanner.registerFilters();
            scanner.doScan(pkg);
        }

    }

}
