package cn.hiboot.mcn.autoconfigure.jdbc;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ConditionalOnMultipleDataSource
 *
 * @author DingHao
 * @since 2022/7/28 17:31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnMultipleDataSource.class)
@interface ConditionalOnMultipleDataSource {

}
