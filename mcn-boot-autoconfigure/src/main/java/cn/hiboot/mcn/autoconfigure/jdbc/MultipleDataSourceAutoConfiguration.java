package cn.hiboot.mcn.autoconfigure.jdbc;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.datasource.lookup.BeanFactoryDataSourceLookup;

import java.util.HashMap;
import java.util.Map;

/**
 * MultipleDataSourceAutoConfiguration
 *
 * @author DingHao
 * @since 2022/7/28 16:00
 */
@AutoConfiguration(after = {DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
@ConditionalOnMultipleDataSource
@ConditionalOnClass(HikariDataSource.class)
@Import(MultipleDataSourceAutoConfiguration.MultipleDataSourceRegister.class)
public class MultipleDataSourceAutoConfiguration {

    static class MultipleDataSourceRegister implements ImportBeanDefinitionRegistrar {

        private final BeanFactory beanFactory;

        public MultipleDataSourceRegister(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            MultipleDataSourceConfig multipleDataSourceConfig = beanFactory.getBean(MultipleDataSourceConfig.class);
            Map<String, DataSourceProperties> properties = multipleDataSourceConfig.getProperties();
            properties.forEach((dsName, ds) -> {
                String dataSourceName = ConfigProperties.getDataSourceBeanName(dsName);
                registry.registerBeanDefinition(dataSourceName, BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class, () -> createDataSource(ds)).getBeanDefinition());
            });
            if(multipleDataSourceConfig.enableDynamicDatasource()){
                registry.registerBeanDefinition("defaultRoutingDataSource"
                        ,BeanDefinitionBuilder.genericBeanDefinition(MultipleDataSourceRegister.class).setFactoryMethod("defaultRoutingDataSource")
                                        .addConstructorArgValue(multipleDataSourceConfig).addConstructorArgValue(beanFactory).setPrimary(true).getBeanDefinition());
                registry.registerBeanDefinition(SwitchSourceAdvisor.class.getName()
                        ,BeanDefinitionBuilder.genericBeanDefinition(SwitchSourceAdvisor.class).setRole(BeanDefinition.ROLE_INFRASTRUCTURE).getBeanDefinition());
            }
        }

        static AbstractRoutingDataSource defaultRoutingDataSource(MultipleDataSourceConfig multipleDataSourceConfig, BeanFactory beanFactory){
            AbstractRoutingDataSource dataSource = new AbstractRoutingDataSource() {
                @Override
                protected Object determineCurrentLookupKey() {
                    return DataSourceHolder.getDataSource();
                }
            };
            dataSource.setDataSourceLookup(new BeanFactoryDataSourceLookup(beanFactory));
            String defaultDataSource = null;
            Map<Object, Object> dataSourceMap = new HashMap<>();
            for (String s : multipleDataSourceConfig.getProperties().keySet()) {
                String beanName = ConfigProperties.getDataSourceBeanName(s);
                if(defaultDataSource == null){
                    defaultDataSource = beanName;
                }
                dataSourceMap.put(s,beanName);
            }
            dataSource.setTargetDataSources(dataSourceMap);
            dataSource.setDefaultTargetDataSource(defaultDataSource);
            return dataSource;
        }

        private HikariDataSource createDataSource(DataSourceProperties dataSourceProperties) {
            return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        }

    }

}