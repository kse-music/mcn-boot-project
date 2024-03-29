package cn.hiboot.mcn.autoconfigure.condition;

import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ConditionalOnPropertyValueNumber
 *
 * @author DingHao
 * @since 2022/1/7 10:04
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnPropertyValueNumber.class)
public @interface ConditionalOnPropertyValueNumber {

    @AliasFor("name")
    String value() default "";

    String prefix() default "";

    @AliasFor("value")
    String name() default "";

    int min() default 1;

    int max() default 10;

}
