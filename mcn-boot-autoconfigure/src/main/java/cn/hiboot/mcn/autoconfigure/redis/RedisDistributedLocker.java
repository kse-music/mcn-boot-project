package cn.hiboot.mcn.autoconfigure.redis;

import cn.hiboot.mcn.core.exception.ServiceException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * RedisDistributedLocker
 *
 * @author DingHao
 * @since 2021/10/21 22:51
 */
public class RedisDistributedLocker implements DistributedLocker{

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> redisScript;

    public RedisDistributedLocker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisScript = new DefaultRedisScript<>();
        this.redisScript.setResultType(Long.class);
        this.redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("unlock.lua")));
    }

    @Override
    public boolean tryLock(String lockKey,int waitTime, int leaseTime,TimeUnit unit) {
        Boolean success = setIfAbsent(lockKey,leaseTime, unit);
        long nanoWaitForLock = unit.toNanos(DEFAULT_WAIT_TIME);
        long start = System.nanoTime();
        while ((System.nanoTime() - start < nanoWaitForLock) && (success == null || !success)) {
            success = setIfAbsent(lockKey,leaseTime, unit);
            if(success != null && success){
                break;
            }
        }
        if(success != null && success){
            return true;
        }
        throw ServiceException.newInstance("获取锁超时");
    }

    private Boolean setIfAbsent(String lockKey, int leaseTime, TimeUnit unit){
        return redisTemplate.opsForValue().setIfAbsent(lockKey,DEFAULT_LOCK_VALUE, leaseTime, unit);
    }

    @Override
    public void unlock(String lockKey) {
        redisTemplate.execute(redisScript, Collections.singletonList(lockKey), DEFAULT_LOCK_VALUE);
    }

}
