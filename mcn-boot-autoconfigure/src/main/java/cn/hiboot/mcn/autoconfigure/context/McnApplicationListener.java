package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.autoconfigure.bootstrap.LogFileChecker;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.*;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 监听器
 * 通过事件以及发生时间的事件源过滤出自己感兴趣的事件
 *
 * @author DingHao
 * @since 2021/1/16 16:28
 */
public class McnApplicationListener implements GenericApplicationListener {

    private final Logger log = LoggerFactory.getLogger(McnApplicationListener.class);

    private static final String DECRYPTED_PROPERTY_SOURCE_NAME = "decrypted";
    private static final String SECURITY_CONTEXT_HOLDER_STRATEGY_SYSTEM_PROPERTY = "spring.security.strategy";

    public static final int DEFAULT_ORDER = LoggingApplicationListener.DEFAULT_ORDER + 1;

    private static final Class<?>[] EVENT_TYPES = { SpringApplicationEvent.class};
    private static final Class<?>[] SOURCE_TYPES = { SpringApplication.class };

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if(applicationEvent instanceof ApplicationStartingEvent event){
            onApplicationStartingEvent(event);
        }else if(applicationEvent instanceof ApplicationEnvironmentPreparedEvent event){
            onApplicationEnvironmentPreparedEvent(event);
        }else if(applicationEvent instanceof ApplicationContextInitializedEvent event){
            onApplicationContextInitializedEvent(event);
        }else if(applicationEvent instanceof ApplicationPreparedEvent event){
            onApplicationPreparedEvent(event);
        }else if(applicationEvent instanceof ApplicationReadyEvent event){
            onApplicationReadyEvent(event);
        }
    }

    private void onApplicationStartingEvent(ApplicationStartingEvent event) {
        //config security holder
        if (!StringUtils.hasText(System.getProperty(SECURITY_CONTEXT_HOLDER_STRATEGY_SYSTEM_PROPERTY))) {
            if(ClassUtils.isPresent("org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken",null)
                    && ClassUtils.isPresent("feign.Feign",null)){
                System.setProperty(SECURITY_CONTEXT_HOLDER_STRATEGY_SYSTEM_PROPERTY,"MODE_INHERITABLETHREADLOCAL");
            }
        }
        //register repeat log file checker
        event.getBootstrapContext().registerIfAbsent(LogFileChecker.class, BootstrapRegistry.InstanceSupplier.of(new LogFileChecker()));

    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        ConfigurableBootstrapContext bootstrapContext = event.getBootstrapContext();
        //config log file checker
        bootstrapContext.getOrElse(LogFileChecker.class, new LogFileChecker()).setEnvironment(environment);
    }

    private void onApplicationContextInitializedEvent(ApplicationContextInitializedEvent event){
        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        //print all EnumerablePropertySource
        if(environment.getProperty("mcn.print-env.enabled",Boolean.class,false)){
            for (PropertySource<?> propertySource : environment.getPropertySources()) {
                String name = propertySource.getName();
                if(!(propertySource instanceof EnumerablePropertySource)){
                    System.out.println();
                    log.info("skip propertySource name = {} because it's not enumerable", name);
                    continue;
                }
                String[] propertyNames = ((EnumerablePropertySource<?>) propertySource).getPropertyNames();
                if(propertyNames.length == 0){
                    System.out.println();
                    log.info("ignore propertySource name = {} because no config property",name);
                    continue;
                }
                System.out.println();
                log.info("start print ------------ {} ------------ ",name);
                for (String propertyName : propertyNames) {
                    log.info("{} = {}",propertyName,propertySource.getProperty(propertyName));
                }
            }
        }
    }

    private void onApplicationPreparedEvent(ApplicationPreparedEvent event){
        //set context to SpringBeanUtils
        SpringBeanUtils.setApplicationContext(event.getApplicationContext());
    }

    private void onApplicationReadyEvent(ApplicationReadyEvent event) {
        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        //Whether to delete the configuration after decryption
        if(environment.getProperty("erase.decrypted-data.enabled",boolean.class,false)){
            environment.getPropertySources().remove(DECRYPTED_PROPERTY_SOURCE_NAME);
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
        return DEFAULT_ORDER;
    }

}