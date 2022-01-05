package cn.hiboot.mcn.autoconfigure.mybatis;

import cn.hiboot.mcn.core.config.McnConstant;
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
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;

import java.io.IOException;

/**
 * MultipleDataSourceAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/2 22:21
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class,HikariDataSource.class})
@Conditional(MultipleDataSourceAutoConfiguration.MultipleDataSourceCondition.class)
public class MultipleDataSourceAutoConfiguration implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {

    private static final String MULTIPLY_DATASOURCE_CONFIG_KEY = "multiply.datasource.name";

    private ResourceLoader resourceLoader;
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry){
        String[] dbs = environment.getProperty(MULTIPLY_DATASOURCE_CONFIG_KEY, String[].class,new String[0]);
        String basePackage = environment.getProperty(McnConstant.APP_BASE_PACKAGE);
        ResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();

        for (String dsName : dbs) {
            String sqlSessionFactoryName = dsName + "SqlSessionFactory";
            scanMapper(registry,sqlSessionFactoryName,basePackage + ".dao." + dsName);

            registry.registerBeanDefinition(dsName + "sqlSessionTemplate", BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class)
                    .setRole(BeanDefinition.ROLE_INFRASTRUCTURE).setSynthetic(true).addConstructorArgReference(sqlSessionFactoryName).getBeanDefinition());

            String dataSourceName = dsName + "DataSource";
            registry.registerBeanDefinition(dataSourceName, BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class,() -> createDataSource(dsName))
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

        }
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

    private HikariDataSource createDataSource(String dsName) {
        DataSourceProperties dataSourceProperties = Binder.get(environment).bind("spring.datasource." + dsName, Bindable.of(DataSourceProperties.class)).get();
        return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    protected static class MultipleDataSourceCondition extends SpringBootCondition {
        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String[] dbs = context.getEnvironment().getProperty(MULTIPLY_DATASOURCE_CONFIG_KEY, String[].class);
            if(dbs != null && dbs.length > 0){
                return ConditionOutcome.match("match multiple datasource");
            }
            return ConditionOutcome.noMatch("no multiple datasource");
        }
    }
}
