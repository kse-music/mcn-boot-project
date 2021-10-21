package cn.hiboot.mcn.autoconfigure.redis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RepeatCommit
 *
 * @author DingHao
 * @since 2021/10/21 23:31
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatCommit {
    /**
     * 防重复提交间隔
     * @return 时间 默认1000ms
     */
    int value() default 1000;
}
