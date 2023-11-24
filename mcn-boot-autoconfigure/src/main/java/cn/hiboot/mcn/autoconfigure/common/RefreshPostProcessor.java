package cn.hiboot.mcn.autoconfigure.common;

import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.util.function.BiConsumer;

/**
 * execute after all singleton instantiated
 * Suitable for instantiating other beans during BeanPostProcessor initialization to avoid BeanPostProcessorChecker warning
 *
 * @author DingHao
 * @since 2023/11/24 18:09
 */
public interface RefreshPostProcessor extends SmartInitializingSingleton {

    @Override
    default void afterSingletonsInstantiated() {
        process();
    }

    void process();

    static <T,R> void uniqueExecute(Class<T> first, Class<R> second, BiConsumer<T,R> consumer){
        ApplicationContext applicationContext = SpringBeanUtils.getApplicationContext();
        T f = applicationContext.getBeanProvider(first).getIfUnique();
        R s = applicationContext.getBeanProvider(second).getIfUnique();
        if (f == null || s == null) {
            return;
        }
        consumer.accept(f,s);
    }

}
