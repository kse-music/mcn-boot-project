package cn.hiboot.mcn.autoconfigure.mybatis;

import cn.hiboot.mcn.core.config.McnConstant;
import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;

/**
 * MultipleDataSourceBeanFactoryPostProcessor
 *
 * @author DingHao
 * @since 2022/1/2 22:21
 */
@ConditionalOnClass(HikariDataSource.class)
@ConditionalOnProperty(MybatisQuickAutoConfiguration.MULTIPLY_DATASOURCE_CONFIG_KEY)
public class MultipleDataSourceConfiguration implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {

    private ResourceLoader resourceLoader;
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry){
        String[] dbs = environment.getProperty(MybatisQuickAutoConfiguration.MULTIPLY_DATASOURCE_CONFIG_KEY, String[].class);
        if(dbs == null || dbs.length == 0 || dbs.length == 1){//只支持一个以上的数据源
            return;
        }
        String basePackage = environment.getProperty(McnConstant.APP_BASE_PACKAGE);
        for (String dsName : dbs) {
            String sqlSessionFactoryName = dsName + "SqlSessionFactory";
            scanMapper(registry,sqlSessionFactoryName,basePackage + ".dao." + dsName);

            String dataSourceName = dsName + "DataSource";
            registry.registerBeanDefinition(dataSourceName, BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class,() -> createDataSource(dsName))
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
                                .addPropertyValue("mapperLocations", pathResolver.getResources("classpath:mapper/"+dsName+"/*.xml"))
                                .addPropertyValue("typeAliasesPackage", basePackage + ".bean")
                                .addPropertyValue("typeHandlersPackage", basePackage + ".dao.handler")
                                .addPropertyValue("configuration", conf)
                                .getBeanDefinition());
            } catch (Exception e) {
                //
            }
        }
    }

    private void scanMapper(BeanDefinitionRegistry registry, String sqlSessionFactoryName, String pkg){
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }
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
}
