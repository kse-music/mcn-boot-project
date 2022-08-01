package cn.hiboot.mcn.autoconfigure.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SwitchSource
 *
 * @author DingHao
 * @since 2022/7/28 15:21
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SwitchSource {
    String value();
}