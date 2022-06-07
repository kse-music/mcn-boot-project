package cn.hiboot.mcn.autoconfigure.redis;

import cn.hiboot.mcn.autoconfigure.redis.annotation.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * DistributedLockerAspect
 *
 * @author DingHao
 * @since 2021/10/21 23:41
 */
@Aspect
public class DistributedLockerAspect {

    private final DistributedLocker distributedLocker;

    public DistributedLockerAspect(DistributedLocker distributedLocker) {
        this.distributedLocker = distributedLocker;
    }

    @Pointcut("@annotation(distributedLock)")
    public void pointCut(DistributedLock distributedLock) {
    }

    @Around(value = "pointCut(distributedLock)", argNames = "p,distributedLock")
    public Object around(ProceedingJoinPoint p, DistributedLock distributedLock)  throws Throwable {
        Object obj = null;
        String value = distributedLock.value();
        if(distributedLocker.tryLock(value)){
            obj =  p.proceed();
            distributedLocker.unlock(value);
        }
        return obj;
    }
}
