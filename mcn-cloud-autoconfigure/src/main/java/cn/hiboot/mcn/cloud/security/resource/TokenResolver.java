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
    String TOKEN_PREFIX = "Bearer";

    default String paramName(){
        return DEFAULT_PARAM_NAME;
    }

    default String tokenPrefix(){
        return TOKEN_PREFIX;
    }

    RestResp<LoginRsp> resolve(String apk);

    default String jwtToken(String apk){
        RestResp<LoginRsp> resolve = resolve(apk);
        if(resolve.isSuccess()){
            String token = resolve.getData().getToken();
            token = (token.length() > TOKEN_PREFIX.length() ? token.substring(TOKEN_PREFIX.length()) : token).trim();
            return token;
        }
        return null;
    }

}
