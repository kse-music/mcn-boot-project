package cn.hiboot.mcn.autoconfigure.web.mvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * 将字符串转成对象
 * <p>1.请求时value无用，主要用于kv编码中参数是json字符串的场景，直接转换成参数类型对象</p>
 * <p>2.返回时value可用，默认转map</p>
 *
 * @author DingHao
 * @since 2021/5/9 19:21
 */
@Target({ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrToObj {

    Class<?> value() default Map.class;

}
