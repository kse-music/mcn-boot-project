package cn.hiboot.mcn.cloud.feign;


import cn.hiboot.mcn.core.util.McnUtils;
import feign.Feign;
import feign.Target;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.openfeign.*;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * FeignCircuitBreakerTargeter
 *
 * @author DingHao
 * @since 2021/7/4 10:43
 */
class FeignCircuitBreakerTargeter implements Targeter {

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final boolean circuitBreakerGroupEnabled;
    private final CircuitBreakerNameResolver circuitBreakerNameResolver;

    FeignCircuitBreakerTargeter(CircuitBreakerFactory circuitBreakerFactory, boolean circuitBreakerGroupEnabled, CircuitBreakerNameResolver circuitBreakerNameResolver) {
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.circuitBreakerGroupEnabled = circuitBreakerGroupEnabled;
        this.circuitBreakerNameResolver = circuitBreakerNameResolver;
    }

    @Override
    public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign, FeignContext context, Target.HardCodedTarget<T> target) {
        if (!(feign instanceof FeignCircuitBreaker.Builder)) {
            return feign.target(target);
        }
        FeignCircuitBreaker.Builder builder = (FeignCircuitBreaker.Builder) feign;
        String name = !StringUtils.hasText(factory.getContextId()) ? factory.getName() : factory.getContextId();
        Class<?> fallback = factory.getFallback();
        if (fallback != void.class) {
            return targetWithFallback(name, context, target, builder, fallback);
        }
        Class<?> fallbackFactory = factory.getFallbackFactory();
        if (fallbackFactory != void.class) {
            return targetWithFallbackFactory(name, context, target, builder, fallbackFactory);
        }
        return builder(name, builder).target(target,new GlobalFallBackFactory<>(target));
    }

    private <T> T targetWithFallbackFactory(String feignClientName, FeignContext context, Target.HardCodedTarget<T> target, FeignCircuitBreaker.Builder builder, Class<?> fallbackFactoryClass) {
        FallbackFactory<? extends T> fallbackFactory = (FallbackFactory<? extends T>) getFromContext("fallbackFactory", feignClientName, context, fallbackFactoryClass, FallbackFactory.class);
        return builder(feignClientName, builder).target(target, fallbackFactory);
    }

    private <T> T targetWithFallback(String feignClientName, FeignContext context, Target.HardCodedTarget<T> target, FeignCircuitBreaker.Builder builder, Class<?> fallback) {
        T fallbackInstance = getFromContext("fallback", feignClientName, context, fallback, target.type());
        return builder(feignClientName, builder).target(target, fallbackInstance);
    }

    private <T> T getFromContext(String fallbackMechanism, String feignClientName, FeignContext context, Class<?> beanType, Class<T> targetType) {
        Object fallbackInstance = context.getInstance(feignClientName, beanType);
        if (fallbackInstance == null) {
            throw new IllegalStateException(String.format("No " + fallbackMechanism + " instance of type %s found for feign client %s",beanType, feignClientName));
        }
        if (!targetType.isAssignableFrom(beanType)) {
            throw new IllegalStateException(String.format("Incompatible " + fallbackMechanism + " instance. Fallback/fallbackFactory of type %s is not assignable to %s for feign client %s",beanType, targetType, feignClientName));
        }
        return (T) fallbackInstance;
    }

    private FeignCircuitBreaker.Builder builder(String feignClientName, FeignCircuitBreaker.Builder builder) {
        Map<String, Object> map = McnUtils.put("circuitBreakerFactory", circuitBreakerFactory, "feignClientName", feignClientName,
                "circuitBreakerGroupEnabled", circuitBreakerGroupEnabled, "circuitBreakerNameResolver", circuitBreakerNameResolver);
        ReflectionUtils.doWithLocalMethods(FeignCircuitBreaker.Builder.class, method -> {
            Object arg = map.get(method.getName());
            if(arg != null){
                ReflectionUtils.makeAccessible(method);
                ReflectionUtils.invokeMethod(method,builder,arg);
            }
        });
        return builder;
    }

}
