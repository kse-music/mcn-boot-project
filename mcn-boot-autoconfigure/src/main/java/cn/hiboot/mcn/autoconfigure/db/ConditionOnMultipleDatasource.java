package cn.hiboot.mcn.autoconfigure.db;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多数据源条件配置
 *
 * @author DingHao
 * @since 2021/6/30 15:21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Conditional(OnMultipleDatasourceCondition.class)
public @interface ConditionOnMultipleDatasource {
    String prefix() default "";
    String name() default "";
}
