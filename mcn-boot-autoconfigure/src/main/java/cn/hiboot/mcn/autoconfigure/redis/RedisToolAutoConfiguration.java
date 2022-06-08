package cn.hiboot.mcn.autoconfigure.redis;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于redis的相关工具
 *
 * @author DingHao
 * @since 2021/10/21 22:53
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
public class RedisToolAutoConfiguration {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public DistributedLocker distributedLocker(StringRedisTemplate redisTemplate){
        return new RedisDistributedLocker(redisTemplate);
    }

    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnClass(Aspect.class)
    protected static class RepeatLockConfiguration{

        @Bean
        @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
        public RepeatCommitAspect repeatCommitAspect(ObjectProvider<Identifier> provider, StringRedisTemplate redisTemplate) {
            return new RepeatCommitAspect(provider,redisTemplate);
        }

        @Bean
        public DistributedLockerAspect distributedLockerAspect(DistributedLocker distributedLocker) {
            return new DistributedLockerAspect(distributedLocker);
        }

    }

}
