package cn.hiboot.mcn.autoconfigure.redis;

import java.util.concurrent.TimeUnit;

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

    default boolean tryLock(String lockKey, int waitTime) {
        return tryLock(lockKey,waitTime,DEFAULT_LEASE_TIME);
    }

    default boolean tryLock(String lockKey, int waitTime, int leaseTime) {
        return tryLock(lockKey,waitTime,leaseTime,DEFAULT_TIME_UNIT);
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

    /**
     * 手动释放锁
     * @param lockKey 锁名称
     */
    void unlock(String lockKey);

}
