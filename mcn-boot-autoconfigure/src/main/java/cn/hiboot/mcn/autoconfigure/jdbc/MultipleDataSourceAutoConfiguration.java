package cn.hiboot.mcn.autoconfigure.jdbc;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.BeanFactory;
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

    @Import(DynamicDataSourceConfiguration.class)
    static class MultipleDataSourceRegister implements ImportBeanDefinitionRegistrar {

        private final MultipleDataSourceConfig multipleDataSourceConfig;

        public MultipleDataSourceRegister(BeanFactory beanFactory) {
            this.multipleDataSourceConfig = beanFactory.getBean(MultipleDataSourceConfig.class);
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            Map<String, DataSourceProperties> properties = multipleDataSourceConfig.getProperties();
            properties.forEach((dsName, ds) -> {
                String dataSourceName = ConfigProperties.getDataSourceBeanName(dsName);
                registry.registerBeanDefinition(dataSourceName, BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class, () -> createDataSource(ds)).getBeanDefinition());
            });
        }

        private HikariDataSource createDataSource(DataSourceProperties dataSourceProperties) {
            return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        }

    }

}