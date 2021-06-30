package cn.hiboot.mcn.autoconfigure.db;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 单数据源条件配置
 * 可以不使用@MapperScan注解
 *
 * @author DingHao
 * @since 2021/6/30 15:21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Conditional(OnSingleDatasourceCondition.class)
public @interface ConditionOnSingleDatasource {
}
