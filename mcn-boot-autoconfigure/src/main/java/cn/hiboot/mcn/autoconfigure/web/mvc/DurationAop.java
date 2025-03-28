package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.core.model.HttpTime;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.Pointcuts;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

/**
 * 设置并返回接口执行时间
 *
 * @author DingHao
 * @since 2019/7/13 11:06
 */
@ConditionalOnProperty(prefix = "mcn.execute-time", name = "enabled", havingValue = "true")
public class DurationAop {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor timeAdvisor(){
        return new TimeRecordAdvisor();
    }

    private static class TimeRecordAdvisor extends AbstractPointcutAdvisor {

        private final Pointcut pointcut;

        public TimeRecordAdvisor() {
            Pointcut cpc = new AnnotationMatchingPointcut(Timing.class, true);
            Pointcut mpc = new AnnotationMatchingPointcut(null, Timing.class, true);
            this.pointcut = Pointcuts.union(cpc, mpc);
        }

        @Override
        public Pointcut getPointcut() {
            return pointcut;
        }

        @Override
        public Advice getAdvice() {
            return (MethodInterceptor) invocation -> {
                long start = System.currentTimeMillis();
                Object o = invocation.proceed();
                long duration = System.currentTimeMillis() - start;
                if (o instanceof HttpTime httpTime) {
                    httpTime.setDuration(duration);
                }
                return o;
            };
        }
    }

}
