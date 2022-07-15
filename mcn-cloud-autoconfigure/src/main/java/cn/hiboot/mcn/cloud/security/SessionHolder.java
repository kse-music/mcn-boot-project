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

    static String getUserId() {
        return getUserId("userId");
    }

    static String getUserId(String userIdKey) {
        Object userId = getUserName().get(userIdKey);
        return userId == null ? null : userId.toString();
    }

    static Map<String, Object> getUserName() {
        return getUserName("user_name");
    }

    static Map<String, Object> getUserName(String userNameKey) {
        Jwt jwt = getJwtToken();
        return jwt == null ? Collections.emptyMap() : jwt.getClaimAsMap(userNameKey);
    }

    static Map<String, Object> getClaims() {
        Jwt jwt = getJwtToken();
        return jwt == null ? Collections.emptyMap() : jwt.getClaims();
    }

    static String getBearerToken() {
        String token = getToken();
        return token == null ? null : "Bearer ".concat(token);
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
