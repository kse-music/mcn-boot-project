package cn.hiboot.mcn.autoconfigure.redis;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.core.exception.ServiceException;
import org.springframework.core.NamedThreadLocal;
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
    private final ThreadLocal<LockCount> threadLocal = new NamedThreadLocal<>("RedisDistributedLocker");

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> redisScript;

    public RedisDistributedLocker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisScript = new DefaultRedisScript<>();
        this.redisScript.setResultType(Long.class);
        this.redisScript.setScriptSource(new ResourceScriptSource(ConfigProperties.createResource("unlock.lua",DistributedLocker.class)));
    }

    @Override
    public boolean tryLock(String lockKey,int waitTime, int leaseTime,TimeUnit unit) {
        LockCount lockCount = threadLocal.get();
        if(lockCount != null && lockCount.lockKey.equals(lockKey)){
            lockCount.count++;
            return true;
        }
        Boolean success = setIfAbsent(lockKey,leaseTime, unit);
        long nanoWaitForLock = unit.toNanos(waitTime);
        long start = System.nanoTime();
        while ((System.nanoTime() - start < nanoWaitForLock) && (success == null || !success)) {
            success = setIfAbsent(lockKey,leaseTime, unit);
            if(success != null && success){
                break;
            }
        }
        if(success != null && success){
            threadLocal.set(new LockCount(lockKey));
            return true;
        }
        throw ServiceException.newInstance(waitTime+"s内获取锁超时");
    }

    private Boolean setIfAbsent(String lockKey, int leaseTime, TimeUnit unit){
        return redisTemplate.opsForValue().setIfAbsent(lockKey,lockKey, leaseTime, unit);
    }

    @Override
    public void unlock(String lockKey) {
        LockCount lockCount = threadLocal.get();
        if(lockCount != null && lockCount.lockKey.equals(lockKey)){
            if (--lockCount.count == 0) {
                redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockKey);
                threadLocal.remove();
            }
        }
    }

    private static class LockCount {
        String lockKey;
        int count = 1;
        public LockCount(String lockKey) {
            this.lockKey = lockKey;
        }
    }

}
