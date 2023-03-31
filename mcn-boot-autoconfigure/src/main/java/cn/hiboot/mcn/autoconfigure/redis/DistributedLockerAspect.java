package cn.hiboot.mcn.autoconfigure.redis;

import cn.hiboot.mcn.autoconfigure.redis.annotation.DistributedLock;
import cn.hiboot.mcn.core.exception.ServiceException;
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
        String value = distributedLock.value();
        return distributedLocker.tryLock(value,() -> {
            try {
                return p.proceed();
            } catch (Throwable e) {
                throw ServiceException.newInstance(e);
            }
        });
    }
}
