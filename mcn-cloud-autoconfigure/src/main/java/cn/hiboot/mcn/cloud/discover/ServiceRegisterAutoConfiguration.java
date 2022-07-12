package cn.hiboot.mcn.cloud.discover;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWarDeployment;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
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
@ConditionalOnWarDeployment
public class ServiceRegisterAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired(required = false)
    private AbstractAutoServiceRegistration<?> registration;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (registration != null) {
            registration.start();
        }
    }

}
