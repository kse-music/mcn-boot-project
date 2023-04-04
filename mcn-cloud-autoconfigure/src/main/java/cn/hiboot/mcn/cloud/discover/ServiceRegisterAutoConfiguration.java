package cn.hiboot.mcn.cloud.discover;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

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
@ConditionalOnNotWebApplication
public class ServiceRegisterAutoConfiguration {

    static class ServiceRegisterConfig implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if(bean instanceof AbstractAutoServiceRegistration){
                ((AbstractAutoServiceRegistration<?>) bean).start();
            }
            return bean;
        }

    }

    static class ServiceRegisterCondition extends AnyNestedCondition {

        ServiceRegisterCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnWarDeployment
        static class WarDeploy {

        }

        @ConditionalOnClass(name = "com.huaweicloud.servicecomb.discovery.registry.ServiceCombAutoServiceRegistration")
        static class CseServiceEngine {

        }

    }

}
