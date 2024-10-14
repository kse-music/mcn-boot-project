package cn.hiboot.mcn.cloud.security.token;

/**
 * TokenResolver
 *
 * @author DingHao
 * @since 2024/10/14 11:12
 */
public interface TokenResolver extends HeaderResolver<LoginRsp> {

    default String jwtToken(String apk){
        LoginRsp loginRsp = apply(apk);
        if(loginRsp == null){
            return null;
        }
        String token = loginRsp.getToken();
        token = (token.length() > tokenPrefix().length() ? token.substring(tokenPrefix().length()) : token).trim();
        return token;
    }

}
