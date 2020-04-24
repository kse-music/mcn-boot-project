package cn.hiboot.mcn.autoconfigure.db;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Conditional(OnMultipleDatasourceCondition.class)
public @interface ConditionOnMultipleDatasource {
    String prefix() default "";
    String name() default "";
}
