package cn.hiboot.mcn.autoconfigure.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * MultipleDataSourceAutoConfiguration
 *
 * @author DingHao
 * @since 2022/7/28 16:00
 */
@AutoConfiguration(before = DataSourceAutoConfiguration.class)
@ConditionalOnMultipleDataSource
@ConditionalOnClass(HikariDataSource.class)
@Import(MultipleDataSourceAutoConfiguration.MultipleDataSourceRegister.class)
public class MultipleDataSourceAutoConfiguration {

    static final String DATA_SOURCE = "DataSource";

    @Import(DynamicDataSourceConfiguration.class)
    static class MultipleDataSourceRegister implements ImportBeanDefinitionRegistrar, BeanFactoryAware {
        private BeanFactory beanFactory;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            Map<String, DataSourceProperties> properties = beanFactory.getBean(MultipleDataSourceMarker.class).getProperties();
            properties.forEach((dsName, ds) -> {
                String dataSourceName = dsName + DATA_SOURCE;
                registry.registerBeanDefinition(dataSourceName, BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class, () -> createDataSource(ds)).getBeanDefinition());
            });
        }

        private HikariDataSource createDataSource(DataSourceProperties dataSourceProperties) {
            return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }
    }

}