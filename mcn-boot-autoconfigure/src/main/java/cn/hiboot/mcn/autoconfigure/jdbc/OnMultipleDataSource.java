package cn.hiboot.mcn.autoconfigure.jdbc;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import org.springframework.beans.factory.config.BeanDefinition;
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
        context.getRegistry().registerBeanDefinition(MultipleDataSourceConfig.class.getName(),
                BeanDefinitionBuilder.genericBeanDefinition(MultipleDataSourceConfig.class,() -> new MultipleDataSourceConfig(properties, environment)).setRole(BeanDefinition.ROLE_INFRASTRUCTURE).getBeanDefinition());
        return true;
    }

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.PARSE_CONFIGURATION;
    }

}
