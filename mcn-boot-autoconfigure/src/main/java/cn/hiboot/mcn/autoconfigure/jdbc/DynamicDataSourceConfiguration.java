package cn.hiboot.mcn.autoconfigure.jdbc;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.datasource.lookup.BeanFactoryDataSourceLookup;

import java.util.HashMap;
import java.util.Map;

/**
 * DynamicRoutingDataSource
 *
 * @author DingHao
 * @since 2022/7/28 17:08
 */
@Conditional(DynamicDataSourceConfiguration.OnEnableDynamicDatasource.class)
@Import(SwitchSourceAdvisor.class)
public class DynamicDataSourceConfiguration implements BeanFactoryAware {

    private BeanFactory beanFactory;

    @Bean
    @Primary
    @ConditionalOnBean(MultipleDataSourceConfig.class)
    AbstractRoutingDataSource dynamicDataSource(MultipleDataSourceConfig multipleDataSourceMarker){
        AbstractRoutingDataSource dataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return DataSourceHolder.getDataSource();
            }
        };
        dataSource.setDataSourceLookup(new BeanFactoryDataSourceLookup(beanFactory));
        String defaultDataSource = null;
        Map<Object, Object> dataSourceMap = new HashMap<>();
        for (String s : multipleDataSourceMarker.getProperties().keySet()) {
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

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    static class OnEnableDynamicDatasource implements ConfigurationCondition{

        @Override
        public ConfigurationPhase getConfigurationPhase() {
            return ConfigurationPhase.PARSE_CONFIGURATION;
        }

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return context.getBeanFactory().getBean(MultipleDataSourceConfig.class).enableDynamicDatasource();
        }

    }
}