package cn.hiboot.mcn.cloud.feign;

import feign.Target;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * 全局fallback工厂
 *
 * @author DingHao
 * @since 2021/7/4 10:17
 */
public class GlobalFallBackFactory<T> implements FallbackFactory<T> {

    private final Target<T> target;

    public GlobalFallBackFactory(Target<T> target) {
        this.target = target;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T create(Throwable cause) {
        final Class<T> targetType = target.type();
        final String targetName = target.name();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetType);
        enhancer.setUseCache(true);
        enhancer.setCallback(new GlobalFallback<>(targetType, targetName, cause));
        return (T) enhancer.create();
    }

}
