package cn.hiboot.mcn.cloud.security.configurer;

import cn.hiboot.mcn.cloud.security.SessionHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * ReloadAuthenticationConfigurer
 *
 * @author DingHao
 * @since 2023/1/16 12:15
 */
public class ReloadAuthenticationConfigurer extends AbstractHttpConfigurer<ReloadAuthenticationConfigurer, HttpSecurity> {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        String[] authenticationReloadBeanNames = applicationContext.getBeanNamesForType(AuthenticationReload.class);
        if (authenticationReloadBeanNames.length == 1) {
            AuthenticationReload authenticationReload = applicationContext.getBean(authenticationReloadBeanNames[0], AuthenticationReload.class);
            http.addFilterBefore((request, response, chain) -> {
                reloadAuthentication(SecurityContextHolder.getContext(),authenticationReload);
                chain.doFilter(request,response);
            }, AnonymousAuthenticationFilter.class);
        }
    }

    public static void reloadAuthentication(SecurityContext securityContext, AuthenticationReload authenticationReload){
        if(securityContext == null || authenticationReload == null){
            return;
        }
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> oldPrincipal = (Map<String, Object>) principal;
                Map<String, Object> newPrincipal = authenticationReload.reload(oldPrincipal);
                if(newPrincipal != null){
                    oldPrincipal.putAll(newPrincipal);
                }
            } else if (principal instanceof Jwt) {
                Jwt jwt0 = (Jwt) principal;
                Map<String, Object> oldPrincipal = jwt0.getClaimAsMap(SessionHolder.USER_NAME);
                Map<String, Object> newPrincipal = authenticationReload.reload(oldPrincipal);
                if(newPrincipal != null){
                    oldPrincipal.putAll(newPrincipal);
                    Map<String,Object> claims = new HashMap<>(jwt0.getClaims());
                    claims.put(SessionHolder.USER_NAME,oldPrincipal);
                    JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken)authentication;
                    Jwt jwt = new Jwt(jwt0.getTokenValue(),jwt0.getIssuedAt(),jwt0.getExpiresAt(),jwt0.getHeaders(),claims);
                    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt,jwtAuthenticationToken.getAuthorities());
                    authenticationToken.setDetails(jwtAuthenticationToken.getDetails());
                    securityContext.setAuthentication(authenticationToken);
                }
            }
        }
    }

}