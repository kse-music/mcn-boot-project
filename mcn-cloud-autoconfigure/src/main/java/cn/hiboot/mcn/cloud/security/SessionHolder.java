package cn.hiboot.mcn.cloud.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    String IDENTIFY = "userId";

    static String getUserId() {
        JwtAuthenticationToken jwtAuthenticationToken = getJwtAuthenticationToken();
        return jwtAuthenticationToken == null ? null : jwtAuthenticationToken.getToken().getClaimAsMap("user_name").get(IDENTIFY).toString();
    }

    static Map<String, Object> getClaims() {
        JwtAuthenticationToken jwtAuthenticationToken = getJwtAuthenticationToken();
        return jwtAuthenticationToken == null ? Collections.emptyMap() : jwtAuthenticationToken.getToken().getClaims();
    }

    static String getToken() {
        JwtAuthenticationToken jwtAuthenticationToken = getJwtAuthenticationToken();
        return jwtAuthenticationToken == null ? null : jwtAuthenticationToken.getToken().getTokenValue();
    }

    static JwtAuthenticationToken getJwtAuthenticationToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof JwtAuthenticationToken ? (JwtAuthenticationToken) authentication : null;
    }

}
