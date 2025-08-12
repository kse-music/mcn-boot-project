package cn.hiboot.mcn.core.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ListTypeDeserialize
 *
 * @author DingHao
 * @since 2025/5/26 18:15
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = EmptyStringListDeserializer.class)
public @interface ListTypeDeserialize {

    Class<?> value();

    boolean emptyList() default true;

}