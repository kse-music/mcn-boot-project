package cn.hiboot.mcn.autoconfigure.web.filter.special;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CheckParam
 *
 * @author DingHao
 * @since 2022/6/6 15:10
 */
@Target({ElementType.TYPE,ElementType.PARAMETER,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckParam {

    String value() default "";

}
