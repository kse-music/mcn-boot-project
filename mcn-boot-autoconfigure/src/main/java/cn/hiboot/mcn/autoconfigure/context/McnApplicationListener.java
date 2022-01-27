package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.autoconfigure.bootstrap.LogFileChecker;
import cn.hiboot.mcn.autoconfigure.util.SpringBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * 监听器
 * 通过事件以及发生时间的事件源过滤出自己感兴趣的事件
 *
 * @author DingHao
 * @since 2021/1/16 16:28
 */
public class McnApplicationListener implements GenericApplicationListener {

    private final Logger log = LoggerFactory.getLogger(McnApplicationListener.class);

    public static final int DEFAULT_ORDER = LoggingApplicationListener.DEFAULT_ORDER + 1;

    private static final Class<?>[] EVENT_TYPES = { ApplicationEnvironmentPreparedEvent.class, ApplicationStartedEvent.class};

    private static final Class<?>[] SOURCE_TYPES = { SpringApplication.class };

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if(applicationEvent instanceof ApplicationEnvironmentPreparedEvent){
            ApplicationEnvironmentPreparedEvent event = (ApplicationEnvironmentPreparedEvent) applicationEvent;
            ConfigurableEnvironment environment = event.getEnvironment();
            logPropertySource(environment);
            initLogFileChecker(environment,event.getBootstrapContext());
        }else if(applicationEvent instanceof ApplicationStartedEvent){
            SpringBeanUtils.setApplicationContext(((ApplicationStartedEvent) applicationEvent).getApplicationContext());
        }
    }

    private void logPropertySource(ConfigurableEnvironment environment){
        if(environment.getProperty("mcn.print-env.enable",Boolean.class,false)){
            for (PropertySource<?> propertySource : environment.getPropertySources()) {
                if(!(propertySource instanceof EnumerablePropertySource)){
                    log.info("skip propertySource name = {}",propertySource.getName());
                    continue;
                }
                System.out.println();
                log.info("start print ------------ {} ------------ ",propertySource.getName());
                for (String propertyName : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
                    log.info("{} = {}",propertyName,propertySource.getProperty(propertyName));
                }
            }
        }
    }

    private void initLogFileChecker(ConfigurableEnvironment environment, ConfigurableBootstrapContext context){
        if(environment.getProperty("delete.default.log-file.enable", Boolean.class, true)){
            context.registerIfAbsent(LogFileChecker.class, BootstrapRegistry.InstanceSupplier.of(new LogFileChecker(environment)));
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
