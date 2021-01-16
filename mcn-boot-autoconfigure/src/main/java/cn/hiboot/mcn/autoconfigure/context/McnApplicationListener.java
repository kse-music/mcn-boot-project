package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.autoconfigure.web.util.SpringBeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.*;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

/**
 * 监听器
 * 通过事件以及发生时间的事件源过滤出自己感兴趣的事件
 *
 * @author DingHao
 * @since 2021/1/16 16:28
 */
public class McnApplicationListener implements GenericApplicationListener {

    public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    private static final Class<?>[] EVENT_TYPES = { ApplicationStartingEvent.class, ApplicationEnvironmentPreparedEvent.class,
            ApplicationPreparedEvent.class, ApplicationStartedEvent.class,ApplicationFailedEvent.class};

    private static final Class<?>[] SOURCE_TYPES = { SpringApplication.class };

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if(applicationEvent instanceof ApplicationStartingEvent){

        }else if(applicationEvent instanceof ApplicationEnvironmentPreparedEvent){

        }else if(applicationEvent instanceof ApplicationPreparedEvent){

        }else if(applicationEvent instanceof ApplicationStartedEvent){
            SpringBeanUtils.setApplicationContext(((ApplicationStartedEvent) applicationEvent).getApplicationContext());
        }else if(applicationEvent instanceof ApplicationFailedEvent){

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
