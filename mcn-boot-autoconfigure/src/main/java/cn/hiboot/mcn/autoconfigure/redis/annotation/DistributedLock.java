package cn.hiboot.mcn.autoconfigure.redis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DistributedLock
 *
 * @author DingHao
 * @since 2021/10/21 23:32
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 设置锁名称
     * @return 锁名称
     */
    String value();
}
