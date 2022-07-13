package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.core.model.result.RestResp;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

/**
 * 设置并返回接口执行时间
 *
 * @author DingHao
 * @since 2019/7/13 11:06
 */
public class DurationAop {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    Advisor timeAdvisor(){
        return new TimeRecordAdvisor();
    }

    private static class TimeRecordAdvisor extends AbstractPointcutAdvisor {

        @Override
        public Pointcut getPointcut() {
            return new ClassOrMethodAnnotationMatchingPointcut(Timing.class);
        }

        @Override
        public Advice getAdvice() {
            return (MethodInterceptor) invocation -> {
                long start = System.currentTimeMillis();
                Object o = invocation.proceed();
                long duration = System.currentTimeMillis() - start;
                if (o instanceof RestResp) {
                    RestResp r = (RestResp) o;
                    r.setDuration(duration);
                }
                return o;
            };
        }
    }

}
