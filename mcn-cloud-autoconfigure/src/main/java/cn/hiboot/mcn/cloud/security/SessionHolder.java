package cn.hiboot.mcn.cloud.security;

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
    String BEARER_PREFIX = "Bearer ";
    String USER_NAME = "user_name";

    static String getUserId() {
        return get("userId");
    }

    static String get(String userIdKey) {
        Object userId = getUser().get(userIdKey);
        return userId == null ? null : userId.toString();
    }

    static Map<String, Object> getUser() {
        return getUser(USER_NAME);
    }

    static Map<String, Object> getUser(String userNameKey) {
        Jwt jwt = getJwtToken();
        return jwt == null ? Collections.emptyMap() : jwt.getClaimAsMap(userNameKey);
    }

    static Map<String, Object> getClaims() {
        Jwt jwt = getJwtToken();
        return jwt == null ? Collections.emptyMap() : jwt.getClaims();
    }

    static String getBearerToken() {
        String token = getToken();
        return token == null ? null : BEARER_PREFIX.concat(token);
    }

    static String getToken() {
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
