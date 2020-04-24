package cn.hiboot.mcn.autoconfigure.db;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import java.util.Map;

class OnMultipleDatasourceCondition implements Condition {

    public static final String BEAN_NAME = MultipleDataSourceRegistryPostProcessor.class.getPackage().getName()+".multiplyDataSourceRegistryPostProcessor";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionOnMultipleDatasource.class.getName());
        String prefix = attributes.get("prefix").toString();
        String name = attributes.get("name").toString();
        if(!StringUtils.hasText(prefix) || !StringUtils.hasText(name)){
            return false;
        }
        Environment environment = context.getEnvironment();
        String[] dbs = environment.getProperty(prefix+"."+name, String[].class);
        if(dbs == null || dbs.length <= 1) {
            return false;
        }
        //register multiply datasource post processor
        BeanDefinitionRegistry registry = context.getRegistry();
        if(!registry.containsBeanDefinition(BEAN_NAME)){
            RootBeanDefinition beanDefinition = new RootBeanDefinition(MultipleDataSourceRegistryPostProcessor.class);
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(environment);
            registry.registerBeanDefinition(BEAN_NAME,beanDefinition);
        }
        return true;
    }

}
