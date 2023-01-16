package cn.hiboot.mcn.cloud.security.configurer;

import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.Map;

/**
 * ReloadAuthenticationConfigurer
 *
 * @author DingHao
 * @since 2023/1/16 12:15
 */
public class ReloadAuthenticationConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>{

    private final ApplicationContext applicationContext;

    public ReloadAuthenticationConfigurer(HttpSecurity httpSecurity) {
        this.applicationContext = httpSecurity.getSharedObject(ApplicationContext.class);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        String[] authenticationReloadBeanNames = applicationContext.getBeanNamesForType(AuthenticationReload.class);
        if (authenticationReloadBeanNames.length == 1) {
            AuthenticationReload authenticationReload = applicationContext.getBean(authenticationReloadBeanNames[0], AuthenticationReload.class);
            http.addFilterBefore((request, response, chain) -> {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null) {
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof Map) {//resource sever
                        @SuppressWarnings("unchecked")
                        Map<String, Object> oldPrincipal = (Map<String, Object>) principal;
                        Map<String, Object> newPrincipal = authenticationReload.reload(oldPrincipal);
                        if(newPrincipal != null){
                            oldPrincipal.putAll(newPrincipal);
                        }
                    }
                }
                chain.doFilter(request,response);
            }, AnonymousAuthenticationFilter.class);
        }
    }

}