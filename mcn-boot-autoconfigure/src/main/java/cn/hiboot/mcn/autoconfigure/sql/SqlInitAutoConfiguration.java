package cn.hiboot.mcn.autoconfigure.sql;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.init.DatabasePopulator;

/**
 * SqlInitAutoConfiguration
 *
 * @author DingHao
 * @since 2022/10/10 14:17
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(SqlInitProperties.class)
@ConditionalOnClass(DatabasePopulator.class)
@Import(DataSourceInitializerInvoker.class)
@ConditionalOnBean(name = SqlInitAutoConfiguration.BEAN_NAME)
public class SqlInitAutoConfiguration {

    static final String BEAN_NAME = "dataSourceInitializerPostProcessor";

    @Bean
    BeanFactoryPostProcessor dataSourceScriptDatabaseInitializerBeanFactoryPostProcessor() {
        return beanFactory -> {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(BEAN_NAME);
            if(beanDefinition instanceof AbstractBeanDefinition){
                ((AbstractBeanDefinition) beanDefinition).setBeanClass(DataSourceInitializerPostProcessor.class);
            }
        };
    }


}