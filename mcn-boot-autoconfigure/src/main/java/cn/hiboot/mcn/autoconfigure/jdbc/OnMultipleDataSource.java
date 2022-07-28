package cn.hiboot.mcn.autoconfigure.jdbc;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.core.util.McnAssert;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * OnMultipleDataSource
 *
 * @author DingHao
 * @since 2022/7/28 17:32
 */
class OnMultipleDataSource implements ConfigurationCondition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        Map<String, DataSourceProperties> properties = Binder.get(environment).bind(ConfigProperties.MULTIPLE_DATASOURCE_PREFIX, Bindable.mapOf(String.class, DataSourceProperties.class)).orElse(null);
        if(properties == null){
            return false;
        }
        checkConfig(environment);
        context.getRegistry().registerBeanDefinition("MultipleDataSourceMarker",
                BeanDefinitionBuilder.genericBeanDefinition(MultipleDataSourceMarker.class,() -> new MultipleDataSourceMarker(properties)).getBeanDefinition());
        return true;
    }

    private void checkConfig(Environment environment) {
        int vote = 0;
        if(environment.getProperty(ConfigProperties.JPA_MULTIPLE_DATASOURCE_PREFIX+".enable",Boolean.class,false)){
            vote++;
        }
        if(environment.getProperty(ConfigProperties.MYBATIS_MULTIPLE_DATASOURCE_PREFIX+".enable",Boolean.class,false)){
            vote++;
        }
        if(environment.getProperty(ConfigProperties.DYNAMIC_DATASOURCE_PREFIX+".enable",Boolean.class,false)){
            vote++;
        }
        McnAssert.state(vote == 1,"mybatis and jpa multiple datasource and dynamic datasource only config one!");
    }

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.PARSE_CONFIGURATION;
    }

}
