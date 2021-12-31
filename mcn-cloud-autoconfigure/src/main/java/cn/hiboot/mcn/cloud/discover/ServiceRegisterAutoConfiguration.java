package cn.hiboot.mcn.cloud.discover;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * ServiceRegisterAutoConfiguration
 *
 * @author DingHao
 * @since 2021/12/31 14:16
 */
@Configuration(proxyBeanMethods = false)
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
