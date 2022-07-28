package cn.hiboot.mcn.autoconfigure.jdbc;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * SwitchSourceConfiguration
 *
 * @author DingHao
 * @since 2022/7/28 17:05
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SwitchSourceAdvisor extends AbstractPointcutAdvisor {

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
            SwitchSource annotation = invocation.getMethod().getAnnotation(SwitchSource.class);
            if(annotation == null){
                annotation = invocation.getMethod().getDeclaringClass().getAnnotation(SwitchSource.class);
            }
            DataSourceHolder.setDataSource(annotation.value());
            Object o = invocation.proceed();
            DataSourceHolder.clearDataSource();
            return o;
        };
    }

}
