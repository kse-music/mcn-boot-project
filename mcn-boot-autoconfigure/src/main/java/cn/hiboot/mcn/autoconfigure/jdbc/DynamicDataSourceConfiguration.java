package cn.hiboot.mcn.autoconfigure.jdbc;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
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
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = ConfigProperties.DYNAMIC_DATASOURCE_PREFIX,name = "enable",havingValue = "true")
@Import(SwitchSourceAdvisor.class)
public class DynamicDataSourceConfiguration implements BeanFactoryAware {

    private BeanFactory beanFactory;

    @Bean
    @Primary
    @ConditionalOnBean(MultipleDataSourceMarker.class)
    AbstractRoutingDataSource dynamicDataSource(MultipleDataSourceMarker multipleDataSourceMarker){
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
}