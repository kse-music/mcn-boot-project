package cn.hiboot.mcn.autoconfigure.web.security.neutral;

import cn.hiboot.mcn.core.util.McnAssert;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NeutralAuthenticationConfigurer
 *
 * 配置放开的url
 *
 * @author DingHao
 * @since 2022/6/12 0:04
 */
public class NeutralAuthenticationConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();
    private List<String> urls = new ArrayList<>();
    private RequestMatcher requestMatcher;

    @Override
    public void init(HttpSecurity http) throws Exception {
        requestMatcher = new OrRequestMatcher(urls.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList()));
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter((request, response, chain) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authenticationTrustResolver.isAnonymous(authentication) && requestMatcher.matches((HttpServletRequest) request)){
                SecurityContextHolder.getContext().setAuthentication(new NeutralAuthentication());
            }
            chain.doFilter(request,response);
        }, AnonymousAuthenticationFilter.class);
    }

    public NeutralAuthenticationConfigurer addUrl(String... url){
        Collections.addAll(urls,url);
        return this;
    }

    public NeutralAuthenticationConfigurer setUrl(List<String> urls){
        McnAssert.notNull(urls,"urls must not null");
        this.urls = urls;
        return this;
    }

    public NeutralAuthenticationConfigurer authenticationTrustResolver(AuthenticationTrustResolver authenticationTrustResolver) {
        McnAssert.notNull(authenticationTrustResolver,"authenticationTrustResolver must not null");
        this.authenticationTrustResolver = authenticationTrustResolver;
        return this;
    }
}
