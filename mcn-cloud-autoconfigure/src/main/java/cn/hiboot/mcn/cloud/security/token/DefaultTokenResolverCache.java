package cn.hiboot.mcn.cloud.security.token;

import cn.hiboot.mcn.core.cache.LocalCache;

/**
 * DefaultTokenResolverCache
 *
 * @author DingHao
 * @since 2024/10/14 11:16
 */
public class DefaultTokenResolverCache implements TokenResolverCache {

    private static final LocalCache<String, LoginRsp> CACHE = new LocalCache<>();

    private final long activity;

    public DefaultTokenResolverCache(long activity) {
        this.activity = activity;
    }

    @Override
    public LoginRsp get(String apk) {
        return CACHE.get(apk);
    }

    @Override
    public LoginRsp put(String apk, LoginRsp result) {
        CACHE.put(apk, result, this.activity);
        return result;
    }

}
