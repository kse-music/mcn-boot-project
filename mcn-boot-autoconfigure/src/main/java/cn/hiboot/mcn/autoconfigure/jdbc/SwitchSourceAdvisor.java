package cn.hiboot.mcn.autoconfigure.jdbc;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SwitchSourceConfiguration
 *
 * @author DingHao
 * @since 2022/7/28 17:05
 */
class SwitchSourceAdvisor extends AbstractPointcutAdvisor {

    private static final Map<Method,String> cache = new ConcurrentHashMap<>();

    private final Pointcut pointcut;

    public SwitchSourceAdvisor() {
        Pointcut cpc = new AnnotationMatchingPointcut(SwitchSource.class, true);
        ComposablePointcut result = new ComposablePointcut(cpc);
        Pointcut mpc = new AnnotationMatchingPointcut(null, SwitchSource.class, true);
        this.pointcut = result.union(mpc);
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return (MethodInterceptor) invocation -> {
            String ds = cache.computeIfAbsent(invocation.getMethod(),m -> {
                SwitchSource annotation = invocation.getMethod().getAnnotation(SwitchSource.class);
                if(annotation == null){
                    annotation = invocation.getMethod().getDeclaringClass().getAnnotation(SwitchSource.class);
                }
                return annotation.value();
            });
            DataSourceHolder.setDataSource(ds);
            Object o = invocation.proceed();
            DataSourceHolder.clearDataSource();
            return o;
        };
    }

}
