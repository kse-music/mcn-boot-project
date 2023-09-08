package cn.hiboot.mcn.autoconfigure.redis;

import cn.hiboot.mcn.autoconfigure.web.filter.common.JsonRequestHelper;
import cn.hiboot.mcn.autoconfigure.web.filter.common.servlet.RequestPayloadRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

/**
 * 基于redis的相关工具
 *
 * @author DingHao
 * @since 2021/10/21 22:53
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisToolAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DistributedLocker distributedLocker(StringRedisTemplate redisTemplate){
        return new RedisDistributedLocker(redisTemplate);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Aspect.class)
    protected static class DistributedLockerAspectConfiguration{

        @Bean
        public DistributedLockerAspect distributedLockerAspect(DistributedLocker distributedLocker) {
            return new DistributedLockerAspect(distributedLocker);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Aspect.class)
    @Import(RepeatCommitAspectConfiguration.JsonDataConveyFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "mcn.repeat", name = "enabled", havingValue = "true")
    protected static class RepeatCommitAspectConfiguration{

        @Bean
        public RepeatCommitAspect repeatCommitAspect(StringRedisTemplate redisTemplate) {
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
