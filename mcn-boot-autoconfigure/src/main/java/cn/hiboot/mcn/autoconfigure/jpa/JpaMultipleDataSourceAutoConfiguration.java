package cn.hiboot.mcn.autoconfigure.jpa;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.jdbc.MultipleDataSourceAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.jdbc.MultipleDataSourceConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.repository.config.*;
import org.springframework.data.util.Streamable;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JpaMultipleDataSourceAutoConfiguration
 *
 * @author DingHao
 * @since 2022/7/26 14:45
 */
@AutoConfiguration(after = {MultipleDataSourceAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
@ConditionalOnProperty(prefix = ConfigProperties.JPA_MULTIPLE_DATASOURCE_PREFIX,name = "enabled",havingValue = "true")
@ConditionalOnClass(JpaRepository.class)
@ConditionalOnBean(MultipleDataSourceConfig.class)
@Import(JpaMultipleDataSourceAutoConfiguration.JpaRepositoriesRegistrar.class)
public class JpaMultipleDataSourceAutoConfiguration {

    static class JpaRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

        private final Environment environment;
        private final ResourceLoader resourceLoader;
        private final MultipleDataSourceConfig multipleDataSourceConfig;

        public JpaRepositoriesRegistrar(Environment environment, ResourceLoader resourceLoader, BeanFactory beanFactory) {
            this.environment = environment;
            this.resourceLoader = resourceLoader;
            this.multipleDataSourceConfig = beanFactory.getBean(MultipleDataSourceConfig.class);
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator generator) {
            String basePackage = multipleDataSourceConfig.getBasePackage();
            generator = new FullyQualifiedAnnotationBeanNameGenerator();//与标准jpa区分
            CustomAnnotationRepositoryConfigurationSource configurationSource = getConfigurationSource(registry,generator);
            JpaProperties jpaProperties = Binder.get(environment).bind("spring.jpa", JpaProperties.class).orElse(new JpaProperties());
            RepositoryConfigurationExtension extension = getExtension();
            RepositoryConfigurationUtils.exposeRegistration(extension, registry, configurationSource);
            RepositoryConfigurationDelegate delegate = new RepositoryConfigurationDelegate(configurationSource, resourceLoader, environment);
            multipleDataSourceConfig.getProperties().forEach((dsName,ds) -> {
                String entityManagerFactoryRef = dsName + "EntityManagerFactory";
                String transactionManagerRef = dsName + "TransactionManager";
                String daoPackage = basePackage + "." + multipleDataSourceConfig.getDaoPackageName() + "." + dsName;

                //JpaConfiguration
                String configBeanName = dsName + "JpaConfiguration";
                registry.registerBeanDefinition(configBeanName,BeanDefinitionBuilder.genericBeanDefinition(JpaConfiguration.class)
                                .addPropertyReference("dataSource",ConfigProperties.getDataSourceBeanName(dsName))
                                .addPropertyValue("packages",basePackage + ".bean")
                                .addPropertyValue("persistenceUnit",dsName)
                                .addPropertyValue("jpaProperties",jpaProperties)
                                .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                                .getBeanDefinition());

                registry.registerBeanDefinition(entityManagerFactoryRef,BeanDefinitionBuilder.genericBeanDefinition()
                                .setFactoryMethodOnBean("localContainerEntityManagerFactoryBean",configBeanName)
                                .addConstructorArgReference("entityManagerFactoryBuilder")
                                .getBeanDefinition());

                registry.registerBeanDefinition(transactionManagerRef,BeanDefinitionBuilder.genericBeanDefinition()
                        .setFactoryMethodOnBean("transactionManager",configBeanName)
                        .addConstructorArgReference(BeanFactory.FACTORY_BEAN_PREFIX+entityManagerFactoryRef)
                        .getBeanDefinition());

                configurationSource.basePackage(daoPackage).entityManagerFactoryRef(entityManagerFactoryRef).transactionManagerRef(transactionManagerRef);
                delegate.registerRepositoriesIn(registry, extension);
            });

        }

        private CustomAnnotationRepositoryConfigurationSource getConfigurationSource(BeanDefinitionRegistry registry,BeanNameGenerator generator) {
            AnnotationMetadata metadata = AnnotationMetadata.introspect(EnableJpaRepositoriesConfiguration.class);
            return new CustomAnnotationRepositoryConfigurationSource(metadata, getAnnotation(), this.resourceLoader,this.environment, registry,generator);
        }

        @Override
        protected Class<? extends Annotation> getAnnotation() {
            return EnableJpaRepositories.class;
        }

        @Override
        protected RepositoryConfigurationExtension getExtension() {
            return new JpaRepositoryConfigExtension();
        }

        @EnableJpaRepositories
        private static class EnableJpaRepositoriesConfiguration {

        }

        static class CustomAnnotationRepositoryConfigurationSource extends AnnotationRepositoryConfigurationSource {

            private String basePackage;
            private final Map<String,String> map;

            CustomAnnotationRepositoryConfigurationSource(AnnotationMetadata metadata, Class<? extends Annotation> annotation,
                                                          ResourceLoader resourceLoader, Environment environment,
                                                          BeanDefinitionRegistry registry,BeanNameGenerator generator) {
                super(metadata, annotation, resourceLoader, environment, registry, generator);
                map = new HashMap<>();
            }

            @Override
            public Streamable<String> getBasePackages() {
                return Streamable.of(this.basePackage);
            }

            @Override
            public Optional<String> getAttribute(String name) {
                if(map.containsKey(name)){
                    return Optional.ofNullable(map.get(name));
                }
                return super.getAttribute(name);
            }

            public CustomAnnotationRepositoryConfigurationSource basePackage(String basePackage) {
                this.basePackage = basePackage;
                return this;
            }

            public CustomAnnotationRepositoryConfigurationSource entityManagerFactoryRef(String entityManagerFactoryRef) {
                map.put("entityManagerFactoryRef",entityManagerFactoryRef);
                return this;
            }

            public CustomAnnotationRepositoryConfigurationSource transactionManagerRef(String transactionManagerRef) {
                map.put("transactionManagerRef",transactionManagerRef);
                return this;
            }
        }

    }

}
