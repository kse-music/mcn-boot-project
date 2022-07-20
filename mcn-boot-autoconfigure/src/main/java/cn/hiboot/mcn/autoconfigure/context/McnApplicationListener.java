package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.autoconfigure.bootstrap.LogFileChecker;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 监听器
 * 通过事件以及发生时间的事件源过滤出自己感兴趣的事件
 *
 * @author DingHao
 * @since 2021/1/16 16:28
 */
public class McnApplicationListener implements GenericApplicationListener {
    private static final String SECURITY_CONTEXT_HOLDER_STRATEGY_SYSTEM_PROPERTY = "spring.security.strategy";

    private final Logger log = LoggerFactory.getLogger(McnApplicationListener.class);

    public static final int DEFAULT_ORDER = LoggingApplicationListener.DEFAULT_ORDER + 1;

    private static final Class<?>[] EVENT_TYPES = { ApplicationEnvironmentPreparedEvent.class, ApplicationContextInitializedEvent.class, ApplicationPreparedEvent.class};

    private static final Class<?>[] SOURCE_TYPES = { SpringApplication.class };

    private LogFileChecker logFileChecker;

    private static final String CONSOLE_LOG_CHARSET = "CONSOLE_LOG_CHARSET";
    private static final String FILE_LOG_CHARSET = "FILE_LOG_CHARSET";

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if(applicationEvent instanceof ApplicationEnvironmentPreparedEvent){
            ApplicationEnvironmentPreparedEvent event = (ApplicationEnvironmentPreparedEvent) applicationEvent;
            triggerEnvironmentPreparedEvent(event.getEnvironment());
        }else if(applicationEvent instanceof ApplicationContextInitializedEvent){
            logPropertySource(((ApplicationContextInitializedEvent) applicationEvent).getApplicationContext().getEnvironment());
            //invoke LogFileChecker
            if(logFileChecker != null){
                logFileChecker.check();
            }
        }else if(applicationEvent instanceof ApplicationPreparedEvent){
            SpringBeanUtils.setApplicationContext(((ApplicationPreparedEvent) applicationEvent).getApplicationContext());
        }
    }

    private void triggerEnvironmentPreparedEvent(ConfigurableEnvironment environment) {
        registerLogFileChecker(environment);
        configSecurityContextHolderStrategyMode(environment);
        setSystemProperty(environment, CONSOLE_LOG_CHARSET, "logging.charset.console", StandardCharsets.UTF_8.name());
        setSystemProperty(environment, FILE_LOG_CHARSET, "logging.charset.file", StandardCharsets.UTF_8.name());
    }

    protected final void setSystemProperty(PropertyResolver resolver, String systemPropertyName, String propertyName,String defaultValue) {
        String value = resolver.getProperty(propertyName);
        value = (value != null) ? value : defaultValue;
        System.setProperty(systemPropertyName, value);
    }

    private void logPropertySource(ConfigurableEnvironment environment){
        if(environment.getProperty("mcn.print-env.enable",Boolean.class,false)){
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

    private void registerLogFileChecker(ConfigurableEnvironment environment){
        if(environment.getProperty("delete.default.log-file.enable", Boolean.class, true)){
            this.logFileChecker = new LogFileChecker(environment);
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
