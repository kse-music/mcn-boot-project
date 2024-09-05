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
    public void init(HttpSecurity http) throws Exception {
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        String[] beanNames = applicationContext.getBeanNamesForType(AuthenticationReload.class);
        if (beanNames.length == 1) {
            AuthenticationReload authenticationReload = applicationContext.getBean(beanNames[0], AuthenticationReload.class);
            http.setSharedObject(AuthenticationReload.class, authenticationReload);
        }
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        AuthenticationReload authenticationReload = http.getSharedObject(AuthenticationReload.class);
        if (authenticationReload == null) {
            return;
        }
        http.addFilterBefore((request, response, chain) -> {
            reload(SecurityContextHolder.getContext(), authenticationReload);
            chain.doFilter(request, response);
        }, AnonymousAuthenticationFilter.class);
    }

    protected void reload(SecurityContext securityContext, AuthenticationReload authenticationReload) {
        reloadAuthentication(securityContext, authenticationReload);
    }

    public static void reloadAuthentication(SecurityContext securityContext, AuthenticationReload authenticationReload) {
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            return;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            Map<String, Object> oldPrincipal = jwt.getClaimAsMap(SessionHolder.USER_NAME);
            Map<String, Object> newPrincipal = authenticationReload.reload(oldPrincipal);
            if (newPrincipal != null) {
                oldPrincipal.putAll(newPrincipal);
                Map<String, Object> claims = new HashMap<>(jwt.getClaims());
                claims.put(SessionHolder.USER_NAME, oldPrincipal);
                JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
                Jwt newJwt = new Jwt(jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getHeaders(), claims);
                JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(newJwt, jwtAuthenticationToken.getAuthorities());
                authenticationToken.setDetails(jwtAuthenticationToken.getDetails());
                securityContext.setAuthentication(authenticationToken);
            }
        }
    }

}