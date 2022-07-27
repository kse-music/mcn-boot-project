package cn.hiboot.mcn.autoconfigure.jpa;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.mybatis.MybatisMultipleDataSourceAutoConfiguration;
import cn.hiboot.mcn.core.util.McnAssert;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.config.*;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * JpaMultipleDataSourceAutoConfiguration
 *
 * @author DingHao
 * @since 2022/7/26 14:45
 */
@AutoConfiguration(before = { HibernateJpaAutoConfiguration.class, TaskExecutionAutoConfiguration.class })
@ConditionalOnProperty(prefix = "jpa."+ConfigProperties.MULTIPLE_DATASOURCE_PREFIX,name = "enable",havingValue = "true")
@ConditionalOnClass(JpaRepository.class)
@ConditionalOnMissingBean({ JpaRepositoryFactoryBean.class, JpaRepositoryConfigExtension.class })
@Import(JpaMultipleDataSourceAutoConfiguration.JpaRepositoriesRegistrar.class)
public class JpaMultipleDataSourceAutoConfiguration {

    static class JpaRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
        private Environment environment;
        private ResourceLoader resourceLoader;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator generator) {
            String basePackage = environment.getProperty(ConfigProperties.APP_BASE_PACKAGE);
            Map<String, DataSourceProperties> properties = Binder.get(environment).bind(ConfigProperties.MULTIPLE_DATASOURCE_PREFIX, Bindable.mapOf(String.class, DataSourceProperties.class)).orElse(null);
            if(properties == null){
                return;
            }
            McnAssert.notNull(registry, "BeanDefinitionRegistry must not be null");
            McnAssert.notNull(resourceLoader, "ResourceLoader must not be null");
            generator = new FullyQualifiedAnnotationBeanNameGenerator();//支持同名接口
            AnnotationMetadata metadata = AnnotationMetadata.introspect(EnableJpaRepositoriesConfiguration.class);
            AnnotationRepositoryConfigurationSource configurationSource = new AnnotationRepositoryConfigurationSource(metadata,getAnnotation(), resourceLoader, environment, registry, generator);
            AnnotationAttributes annotationAttributes = getAnnotationAttributes(configurationSource);
            if(annotationAttributes == null){
                return;
            }
            JpaProperties jpaProperties = Binder.get(environment).bind("spring.jpa", JpaProperties.class).orElse(new JpaProperties());

            RepositoryConfigurationExtension extension = getExtension();
            RepositoryConfigurationUtils.exposeRegistration(extension, registry, configurationSource);
            RepositoryConfigurationDelegate delegate = new RepositoryConfigurationDelegate(configurationSource, resourceLoader, environment);
            properties.forEach((dsName,ds) -> {
                String entityManagerFactoryRef = dsName + "EntityManagerFactory";
                String transactionManagerRef = dsName + "TransactionManager";

                //override config
                annotationAttributes.put("basePackages",basePackage + ".dao." + dsName);
                annotationAttributes.put("entityManagerFactoryRef",entityManagerFactoryRef);
                annotationAttributes.put("transactionManagerRef",transactionManagerRef);

                //数据源
                String dataSourceName = dsName + "DataSource";
                registry.registerBeanDefinition(dataSourceName, BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class,() ->  MybatisMultipleDataSourceAutoConfiguration.createDataSource(ds)).getBeanDefinition());

                //JpaConfiguration
                String configBeanName = dsName + "JpaConfiguration";
                registry.registerBeanDefinition(configBeanName,BeanDefinitionBuilder.genericBeanDefinition(JpaConfiguration.class)
                                .addPropertyReference("dataSource",dataSourceName)
                                .addPropertyValue("packages",basePackage + ".bean")
                                .addPropertyValue("persistenceUnit",dsName)
                                .addPropertyValue("jpaProperties",jpaProperties)
                                .getBeanDefinition());

                registry.registerBeanDefinition(entityManagerFactoryRef,BeanDefinitionBuilder.genericBeanDefinition(JpaConfiguration.class)
                                .setFactoryMethodOnBean("localContainerEntityManagerFactoryBean",configBeanName)
                                .addConstructorArgReference("entityManagerFactoryBuilder")
                                .getBeanDefinition());

                registry.registerBeanDefinition(transactionManagerRef,BeanDefinitionBuilder.genericBeanDefinition(JpaConfiguration.class)
                        .setFactoryMethodOnBean("transactionManager",configBeanName)
                        .addConstructorArgReference(BeanFactory.FACTORY_BEAN_PREFIX+entityManagerFactoryRef)
                        .getBeanDefinition());

                delegate.registerRepositoriesIn(registry, extension);
            });

        }

        private AnnotationAttributes getAnnotationAttributes(AnnotationRepositoryConfigurationSource configurationSource){
            Field attributes = ReflectionUtils.findField(AnnotationRepositoryConfigurationSource.class, "attributes");
            ReflectionUtils.makeAccessible(attributes);
            try {
                return (AnnotationAttributes) attributes.get(configurationSource);
            } catch (IllegalAccessException e) {
                //ignore
            }
            return null;
        }

        @Override
        protected Class<? extends Annotation> getAnnotation() {
            return EnableJpaRepositories.class;
        }

        @Override
        protected RepositoryConfigurationExtension getExtension() {
            return new JpaRepositoryConfigExtension();
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
            super.setEnvironment(environment);
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
            super.setResourceLoader(resourceLoader);
        }

    }

    @EnableJpaRepositories
    private static class EnableJpaRepositoriesConfiguration {

    }

}
