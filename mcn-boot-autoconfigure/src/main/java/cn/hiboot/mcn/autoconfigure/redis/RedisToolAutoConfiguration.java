package cn.hiboot.mcn.autoconfigure.redis;

import cn.hiboot.mcn.autoconfigure.web.filter.common.JsonRequestHelper;
import cn.hiboot.mcn.autoconfigure.web.filter.common.RequestPayloadRequestWrapper;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 基于redis的相关工具
 *
 * @author DingHao
 * @since 2021/10/21 22:53
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
public class RedisToolAutoConfiguration {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public DistributedLocker distributedLocker(StringRedisTemplate redisTemplate){
        return new RedisDistributedLocker(redisTemplate);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Aspect.class)
    protected static class DistributedLockerAspectConfiguration{

        @Bean
        @ConditionalOnBean(DistributedLocker.class)
        public DistributedLockerAspect distributedLockerAspect(DistributedLocker distributedLocker) {
            return new DistributedLockerAspect(distributedLocker);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Aspect.class)
    @Import(RepeatCommitAspectConfiguration.JsonDataConveyFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "mcn.repeat", name = "enable", havingValue = "true")
    protected static class RepeatCommitAspectConfiguration{

        @Bean
        @ConditionalOnBean(StringRedisTemplate.class)
        public RepeatCommitAspect repeatCommitAspect( StringRedisTemplate redisTemplate) {
            return new RepeatCommitAspect(redisTemplate);
        }

        protected static class JsonDataConveyFilter implements Filter{

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                if(JsonRequestHelper.isJsonRequest(httpServletRequest)){
                    request = new RequestPayloadRequestWrapper(httpServletRequest);
                }
                chain.doFilter(request,response);
            }
        }

    }


}
