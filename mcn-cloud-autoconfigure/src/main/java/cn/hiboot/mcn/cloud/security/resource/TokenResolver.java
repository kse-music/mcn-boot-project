package cn.hiboot.mcn.cloud.security.resource;

import cn.hiboot.mcn.core.model.result.RestResp;

/**
 * TokenResolver
 *
 * @author DingHao
 * @since 2023/2/15 22:29
 */
public interface TokenResolver {

    String DEFAULT_PARAM_NAME = "APK";

    default String paramName(){
        return DEFAULT_PARAM_NAME;
    }

    RestResp<LoginRsp> resolve(String apk);
}
