package cn.hiboot.mcn.autoconfigure.mybatis;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
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
import java.util.Map;

/**
 * MultipleDataSourceAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/2 22:21
 */
@AutoConfiguration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class,HikariDataSource.class})
@ConditionalOnProperty(prefix = "mybatis."+MybatisMultipleDataSourceAutoConfiguration.MULTIPLE_DATASOURCE_PREFIX,name = "enable",havingValue = "true")
@Import(MybatisMultipleDataSourceAutoConfiguration.MultipleDataSourceConfig.class)
public class MybatisMultipleDataSourceAutoConfiguration {

    public static final String MULTIPLE_DATASOURCE_PREFIX = "multiple.datasource";

    protected static class MultipleDataSourceConfig implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {
        private ResourceLoader resourceLoader;
        private Environment environment;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry){
            String basePackage = environment.getProperty(ConfigProperties.APP_BASE_PACKAGE);
            ResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
            Map<String, DataSourceProperties> properties = Binder.get(environment).bind(MULTIPLE_DATASOURCE_PREFIX, Bindable.mapOf(String.class, DataSourceProperties.class)).orElse(null);
            if(properties == null){
                return;
            }
            properties.forEach((dsName,ds) -> {
                String sqlSessionFactoryName = dsName + "SqlSessionFactory";
                scanMapper(registry,sqlSessionFactoryName,basePackage + ".dao." + dsName);

                registry.registerBeanDefinition(dsName + "sqlSessionTemplate", BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class)
                        .setRole(BeanDefinition.ROLE_INFRASTRUCTURE).setSynthetic(true).addConstructorArgReference(sqlSessionFactoryName).getBeanDefinition());

                String dataSourceName = dsName + "DataSource";
                registry.registerBeanDefinition(dataSourceName, BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class,() -> createDataSource(ds))
                        .setRole(BeanDefinition.ROLE_INFRASTRUCTURE).setSynthetic(true).getBeanDefinition());

                SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
                factoryBean.setVfs(SpringBootVFS.class);
                org.apache.ibatis.session.Configuration conf = new org.apache.ibatis.session.Configuration();
                conf.setMapUnderscoreToCamelCase(true);
                registry.registerBeanDefinition(sqlSessionFactoryName,
                        loadMapper(pathResolver, dsName)
                                .addPropertyValue("dataSource", new RuntimeBeanReference(dataSourceName))
                                .addPropertyValue("typeAliasesPackage", basePackage + ".bean" + dsName)
                                .addPropertyValue("typeHandlersPackage", basePackage + ".dao.handler" + dsName)
                                .addPropertyValue("configuration", conf).getBeanDefinition());

            });
        }

        private BeanDefinitionBuilder loadMapper(ResourcePatternResolver pathResolver, String dsName){
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(SqlSessionFactoryBean.class)
                    .setRole(BeanDefinition.ROLE_INFRASTRUCTURE).addPropertyValue("vfs", SpringBootVFS.class);
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

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

    }

    public static HikariDataSource createDataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }


}
