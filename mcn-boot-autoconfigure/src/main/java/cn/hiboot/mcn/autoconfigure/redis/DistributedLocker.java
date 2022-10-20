package cn.hiboot.mcn.autoconfigure.redis;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁
 *
 * @author DingHao
 * @since 2021/10/21 22:47
 */
public interface DistributedLocker{

    TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    int DEFAULT_WAIT_TIME = 5;

    int DEFAULT_LEASE_TIME = 5;

    default boolean tryLock(String lockKey) {
        return tryLock(lockKey,DEFAULT_WAIT_TIME);
    }

    default <V> V tryExecute(String lockKey, Supplier<V> supplier) {
        return tryExecute(lockKey,DEFAULT_WAIT_TIME,supplier);
    }

    default boolean tryLock(String lockKey, int waitTime) {
        return tryLock(lockKey,waitTime,DEFAULT_LEASE_TIME);
    }

    default <V> V tryExecute(String lockKey, int waitTime, Supplier<V> supplier) {
        return tryExecute(lockKey,waitTime,DEFAULT_LEASE_TIME,supplier);
    }

    default boolean tryLock(String lockKey, int waitTime, int leaseTime) {
        return tryLock(lockKey,waitTime,leaseTime,DEFAULT_TIME_UNIT);
    }

    default <V> V tryExecute(String lockKey, int waitTime, int leaseTime, Supplier<V> supplier) {
        return tryExecute(lockKey,waitTime,leaseTime,DEFAULT_TIME_UNIT,supplier);
    }

    /**
     * 获取分布式锁
     * @param lockKey 锁名称
     * @param waitTime 获取锁等待时间 默认5s
     * @param leaseTime 锁持有的时间 默认5s
     * @param unit 锁时间单位 默认秒
     * @return true获取成功 false获取失败
     */
    boolean tryLock(String lockKey,int waitTime, int leaseTime, TimeUnit unit);

    default <V> V tryExecute(String lockKey, int waitTime, int leaseTime, TimeUnit unit, Supplier<V> supplier) {
        if(tryLock(lockKey,waitTime,leaseTime,unit)){
            try {
                return supplier.get();
            }finally {
                unlock(lockKey);
            }
        }
        return null;
    }

    /**
     * 手动释放锁
     * @param lockKey 锁名称
     */
    void unlock(String lockKey);

}
