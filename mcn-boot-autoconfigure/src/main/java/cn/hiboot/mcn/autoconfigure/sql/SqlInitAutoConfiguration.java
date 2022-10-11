package cn.hiboot.mcn.autoconfigure.sql;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.init.DatabasePopulator;

/**
 * SqlInitAutoConfiguration
 *
 * @author DingHao
 * @since 2022/10/10 14:17
 */
@AutoConfiguration(after = SqlInitializationAutoConfiguration.class )
@EnableConfigurationProperties(SqlInitProperties.class)
@ConditionalOnBean(SqlDataSourceScriptDatabaseInitializer.class)
@ConditionalOnClass(DatabasePopulator.class)
public class SqlInitAutoConfiguration {

    @Bean
    BeanPostProcessor dataSourceScriptDatabaseInitializerBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if(bean instanceof SqlDataSourceScriptDatabaseInitializer){
                    return SqlInitCreator.create();
                }
                return bean;
            }

        };
    }

}