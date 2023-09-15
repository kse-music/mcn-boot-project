package cn.hiboot.mcn.cloud.security.resource;

import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * TokenResolver
 *
 * @author DingHao
 * @since 2023/9/15 9:41
 */
public interface ApkResolver {
    String DEFAULT_PARAM_NAME = "APK";
    String TOKEN_PREFIX = "Bearer";

    default String paramName(){
        return DEFAULT_PARAM_NAME;
    }

    default String tokenPrefix(){
        return TOKEN_PREFIX;
    }

    Mono<RestResp<LoginRsp>> resolve(String apk);

    default Mono<Authentication> jwtToken(String apk){
        return resolve(apk).handle((r, sink) -> {
            if(r.isSuccess()) {
                String token = r.getData().getToken();
                token = (token.length() > tokenPrefix().length() ? token.substring(tokenPrefix().length()) : token).trim();
                sink.next(new BearerTokenAuthenticationToken(token));
                return;
            }
            sink.error(ServiceException.newInstance(paramName() + "不正确"));
        });
    }
}
