package cn.hiboot.mcn.cloud.security.token;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DefaultTokenResolverCache
 *
 * @author DingHao
 * @since 2024/10/14 11:16
 */
public class DefaultTokenResolverCache implements TokenResolverCache {

    private static final Map<String, Result> CACHE = new ConcurrentHashMap<>();

    private final long activity;

    public DefaultTokenResolverCache(long activity) {
        this.activity = activity;
    }

    @Override
    public LoginRsp get(String apk) {
        Result tokenResult = CACHE.get(apk);
        if (tokenResult == null || tokenResult.exp < System.currentTimeMillis()) {
            return null;
        }
        return tokenResult.result;
    }

    @Override
    public LoginRsp put(String apk, LoginRsp result) {
        CACHE.put(apk, new Result(result, this.activity));
        return result;
    }

    private static class Result {

        private final LoginRsp result;
        private final long exp;

        public Result(LoginRsp result, Long exp) {
            this.result = result;
            this.exp = System.currentTimeMillis() + exp;
        }

    }

}
