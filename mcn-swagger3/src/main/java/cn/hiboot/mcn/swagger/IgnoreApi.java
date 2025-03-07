package cn.hiboot.mcn.swagger;

import io.swagger.v3.oas.annotations.Hidden;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ignore api create
 *
 * @author DingHao
 * @since 2019/2/15 10:58
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Hidden
public @interface IgnoreApi {
}
