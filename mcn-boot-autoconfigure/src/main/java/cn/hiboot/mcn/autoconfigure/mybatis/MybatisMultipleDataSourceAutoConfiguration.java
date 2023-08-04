package cn.hiboot.mcn.autoconfigure.mybatis;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.jdbc.MultipleDataSourceAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.jdbc.MultipleDataSourceConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
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
@AutoConfiguration(after = {MultipleDataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class,HikariDataSource.class})
@ConditionalOnProperty(prefix = ConfigProperties.MYBATIS_MULTIPLE_DATASOURCE_PREFIX,name = "enable",havingValue = "true")
@ConditionalOnBean(MultipleDataSourceConfig.class)
@Import(MybatisMultipleDataSourceAutoConfiguration.MybatisMultipleDataSourceConfig.class)
public class MybatisMultipleDataSourceAutoConfiguration {

    protected static class MybatisMultipleDataSourceConfig implements ImportBeanDefinitionRegistrar {
        private final ResourceLoader resourceLoader;
        private final String basePackage;
        private final MultipleDataSourceConfig multipleDataSourceConfig;

        public MybatisMultipleDataSourceConfig(ResourceLoader resourceLoader, Environment environment, BeanFactory beanFactory) {
            this.resourceLoader = resourceLoader;
            this.basePackage = environment.getProperty(ConfigProperties.APP_BASE_PACKAGE);
            this.multipleDataSourceConfig = beanFactory.getBean(MultipleDataSourceConfig.class);
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry){
            ResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
            multipleDataSourceConfig.getProperties().forEach((dsName,ds) -> {
                String sqlSessionFactoryName = dsName + "SqlSessionFactory";
                scanMapper(registry,sqlSessionFactoryName,basePackage + "." + multipleDataSourceConfig.getDaoPackageName() + "." + dsName);
                registry.registerBeanDefinition(dsName + "SqlSessionTemplate", BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class)
                        .addConstructorArgReference(sqlSessionFactoryName)
                        .getBeanDefinition());
                registry.registerBeanDefinition(sqlSessionFactoryName,buildDefinition(pathResolver, dsName));

            });
        }

        private BeanDefinition buildDefinition(ResourcePatternResolver pathResolver, String dsName){
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactoryBean.class);
            Resource[] resources = getResources(pathResolver, dsName);
            if(resources != null){
                beanDefinitionBuilder.addPropertyValue("mapperLocations", resources);
            }
            org.apache.ibatis.session.Configuration conf = new org.apache.ibatis.session.Configuration();
            conf.setMapUnderscoreToCamelCase(true);
            beanDefinitionBuilder
                    .addPropertyValue("dataSource", new RuntimeBeanReference(ConfigProperties.getDataSourceBeanName(dsName)))
                    .addPropertyValue("vfs", SpringBootVFS.class)
                    .addPropertyValue("typeAliasesPackage", basePackage + ".bean." + dsName)
                    .addPropertyValue("typeHandlersPackage", basePackage + "." + multipleDataSourceConfig.getDaoPackageName() + ".handler." + dsName)
                    .addPropertyValue("configuration", conf);
            return beanDefinitionBuilder.getBeanDefinition();
        }

        private Resource[] getResources(ResourcePatternResolver pathResolver, String dsName){
            try {
                return pathResolver.getResources("classpath:mapper/" + dsName + "/*.xml");
            } catch (IOException e) {
                return null;
            }
        }

        private void scanMapper(BeanDefinitionRegistry registry, String sqlSessionFactoryName, String pkg){
            ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
            if (resourceLoader != null) {
                scanner.setResourceLoader(resourceLoader);
            }
            scanner.setBeanNameGenerator(FullyQualifiedAnnotationBeanNameGenerator.INSTANCE);
            scanner.setSqlSessionFactoryBeanName(sqlSessionFactoryName);
            scanner.registerFilters();
            scanner.doScan(pkg);
        }

    }

}
