package cn.hiboot.mcn.cloud.security.token;

import cn.hiboot.mcn.cloud.security.resource.LoginRsp;

import java.util.function.Supplier;

/**
 * TokenResolverCache
 *
 * @author DingHao
 * @since 2024/10/14 11:13
 */
public interface TokenResolverCache {

    LoginRsp get(String apk);

    default LoginRsp get(String apk, Supplier<LoginRsp> supplier) {
        LoginRsp result = get(apk);
        if (result == null) {
            result = put(apk, supplier.get());
        }
        return result;
    }

    LoginRsp put(String apk, LoginRsp result);

}
