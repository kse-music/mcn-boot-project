package cn.hiboot.mcn.cloud.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null){
            if(authentication instanceof JwtAuthenticationToken){
                JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
                Map<String, Object> tokenAttributes = jwtAuthenticationToken.getTokenAttributes();
                Map<String,Object> user = (Map<String,Object>)tokenAttributes.get("user_name");
                return user.get(IDENTIFY).toString();
            }
        }
        return null;
    }


    static String getToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null){
            if(authentication instanceof JwtAuthenticationToken){
                JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
                return jwtAuthenticationToken.getToken().getTokenValue();
            }
        }
        return null;
    }

}
