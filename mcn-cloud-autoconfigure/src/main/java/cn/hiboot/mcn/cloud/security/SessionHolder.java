package cn.hiboot.mcn.cloud.security;

import cn.hiboot.mcn.cloud.security.resource.TokenResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;
import java.util.Map;

/**
 * SessionHolder
 *
 * @author DingHao
 * @since 2021/8/20 10:32
 */
public interface SessionHolder {
    String USER_NAME = "user_name";

    static String getUserId() {
        Object value = getUser().get("userId");
        return value == null ? null : value.toString();
    }

    static Map<String, Object> getUser() {
        Jwt jwt = getJwtToken();
        return jwt == null ? Collections.emptyMap() : jwt.getClaimAsMap(USER_NAME);
    }

    static Map<String, Object> getClaims() {
        Jwt jwt = getJwtToken();
        return jwt == null ? Collections.emptyMap() : jwt.getClaims();
    }

    static String getBearerToken() {
        String token = getJwtTokenString();
        return token == null ? null : TokenResolver.TOKEN_PREFIX.concat(" ").concat(token);
    }

    static String getJwtTokenString() {
        Jwt jwt = getJwtToken();
        return jwt == null ? null : jwt.getTokenValue();
    }

    static Jwt getJwtToken() {
        JwtAuthenticationToken jwtAuthenticationToken = getJwtAuthenticationToken();
        return jwtAuthenticationToken == null ? null : jwtAuthenticationToken.getToken();
    }

    static JwtAuthenticationToken getJwtAuthenticationToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof JwtAuthenticationToken ? (JwtAuthenticationToken) authentication : null;
    }

}
