package cn.hiboot.mcn.cloud.security.token;

import java.util.function.Function;

/**
 * HeaderResolver
 *
 * @author DingHao
 * @since 2024/10/14 11:12
 */
interface HeaderResolver<R> extends Function<String, R> {

    String DEFAULT_PARAM_NAME = "APK";
    String TOKEN_PREFIX = "Bearer";

    default String paramName(){
        return DEFAULT_PARAM_NAME;
    }

    default String tokenPrefix(){
        return TOKEN_PREFIX;
    }

}
