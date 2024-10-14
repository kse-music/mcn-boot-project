package cn.hiboot.mcn.cloud.security.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * ServerTokenResolver
 *
 * @author DingHao
 * @since 2024/10/14 11:26
 */
public interface ServerTokenResolver extends HeaderResolver<Mono<LoginRsp>> {

    default Mono<Authentication> jwtToken(String apk){
        return apply(apk).map(rs -> {
            String token = rs.getToken();
            token = (token.length() > tokenPrefix().length() ? token.substring(tokenPrefix().length()) : token).trim();
            return new BearerTokenAuthenticationToken(token);
        });
    }

}
