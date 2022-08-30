package cn.hiboot.mcn.cloud.context;

import cn.hiboot.mcn.autoconfigure.context.McnApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.bootstrap.encrypt.AbstractEnvironmentDecrypt;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * McnCloudApplicationListener
 *
 * @author DingHao
 * @since 2022/8/30 23:02
 */
public class McnCloudApplicationListener implements GenericApplicationListener {

    private static final String SECURITY_CONTEXT_HOLDER_STRATEGY_SYSTEM_PROPERTY = "spring.security.strategy";

    private static final Class<?>[] EVENT_TYPES = { ApplicationEnvironmentPreparedEvent.class,ApplicationReadyEvent.class};

    private static final Class<?>[] SOURCE_TYPES = { SpringApplication.class };

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if(applicationEvent instanceof ApplicationEnvironmentPreparedEvent){
            ApplicationEnvironmentPreparedEvent event = (ApplicationEnvironmentPreparedEvent) applicationEvent;
            configSecurityContextHolderStrategyMode(event.getEnvironment());
        }else if(applicationEvent instanceof ApplicationReadyEvent){
            ApplicationReadyEvent event = (ApplicationReadyEvent) applicationEvent;
            ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
            eraseDecryptedData(environment);
        }
    }

    private void configSecurityContextHolderStrategyMode(ConfigurableEnvironment environment){
        String strategyName = environment.getProperty(SECURITY_CONTEXT_HOLDER_STRATEGY_SYSTEM_PROPERTY);
        if (!StringUtils.hasText(strategyName)) {
            if(ClassUtils.isPresent("org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken",null)
                    && ClassUtils.isPresent("feign.Feign",null)){
                strategyName = "MODE_INHERITABLETHREADLOCAL";
            }
        }
        if (StringUtils.hasText(strategyName)) {
            System.setProperty(SECURITY_CONTEXT_HOLDER_STRATEGY_SYSTEM_PROPERTY,strategyName);
        }
    }

    private void eraseDecryptedData(ConfigurableEnvironment environment) {
        if(environment.getProperty("erase.decrypted-data.enable",boolean.class,false)){
            environment.getPropertySources().remove(AbstractEnvironmentDecrypt.DECRYPTED_PROPERTY_SOURCE_NAME);
        }
    }

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        return isAssignableFrom(resolvableType.getRawClass(), EVENT_TYPES);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return isAssignableFrom(sourceType, SOURCE_TYPES);
    }

    private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
        if (type != null) {
            for (Class<?> supportedType : supportedTypes) {
                if (supportedType.isAssignableFrom(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return McnApplicationListener.DEFAULT_ORDER + 1;
    }

}
