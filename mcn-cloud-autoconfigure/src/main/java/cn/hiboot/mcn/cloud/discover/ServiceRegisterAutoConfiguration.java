package cn.hiboot.mcn.cloud.discover;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;

/**
 * ServiceRegisterAutoConfiguration
 *
 * @author DingHao
 * @since 2021/12/31 14:16
 */
@AutoConfiguration
@ConditionalOnSingleCandidate(AbstractAutoServiceRegistration.class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@Conditional(ServiceRegisterAutoConfiguration.ServiceRegisterCondition.class)
@Import(ServiceRegisterAutoConfiguration.ServiceRegisterConfig.class)
public class ServiceRegisterAutoConfiguration {

    static class ServiceRegisterConfig implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if(bean instanceof AbstractAutoServiceRegistration<?> registration){
                registration.start();
            }
            return bean;
        }

    }

    static class ServiceRegisterCondition extends AnyNestedCondition {

        ServiceRegisterCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @Conditional(OnWarDeploymentCondition.class)
        static class WarDeploy {

        }

        @ConditionalOnClass(name = "com.huaweicloud.servicecomb.discovery.registry.ServiceCombAutoServiceRegistration")
        @ConditionalOnNotWebApplication
        static class CseServiceEngine {//cse不注册上去订阅不到其它服务,nacos则不需要

        }

    }

    static class OnWarDeploymentCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            if(ClassUtils.isPresent("org.springframework.web.context.WebApplicationContext",context.getClassLoader())){
                ResourceLoader resourceLoader = context.getResourceLoader();
                if (resourceLoader instanceof WebApplicationContext applicationContext) {
                    return applicationContext.getServletContext() != null;
                }
            }
            return false;
        }
    }

}
