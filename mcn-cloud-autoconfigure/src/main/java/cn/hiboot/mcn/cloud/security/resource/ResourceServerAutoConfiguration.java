package cn.hiboot.mcn.cloud.security.resource;

import cn.hiboot.mcn.autoconfigure.web.mvc.ResponseUtils;
import cn.hiboot.mcn.cloud.security.configurer.ReloadAuthenticationConfigurer;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ResourceServerAutoConfiguration
 *
 * @author DingHao
 * @since 2023/2/8 15:26
 */
@AutoConfiguration
@EnableConfigurationProperties(ResourceServerProperties.class)
@ConditionalOnClass(JwtAuthenticationToken.class)
@ConditionalOnProperty(value = "spring.security.oauth2.resourceserver.jwt.public-key-location",havingValue = "classpath:config/public.txt")
public class ResourceServerAutoConfiguration {

    private final ResourceServerProperties ssoProperties;

    public ResourceServerAutoConfiguration(ResourceServerProperties ssoProperties) {
        this.ssoProperties = ssoProperties;
    }

    @Bean
    SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        if (McnUtils.isNotNullAndEmpty(ssoProperties.getAllowedPaths())) {
            http.authorizeRequests().antMatchers(ssoProperties.getAllowedPaths().toArray(new String[0])).permitAll();
        }
        return http
                .authorizeRequests((requests) -> requests.anyRequest().authenticated())
                .oauth2ResourceServer(c -> c.jwt().and()
                        .authenticationEntryPoint((request, response, authException) -> handleException(authException,response))
                        .accessDeniedHandler((request, response, accessDeniedException) -> handleException(accessDeniedException,response))
                )
                .apply(new ReloadAuthenticationConfigurer()).and()
                .build();
    }

    private void handleException(RuntimeException exception, HttpServletResponse response){
        if(exception instanceof AuthenticationException){
            if(exception instanceof OAuth2AuthenticationException){
                ResponseUtils.failed(exception.getMessage(),response);
            }else {
                ResponseUtils.failed(ExceptionKeys.HTTP_ERROR_401,response);
            }
        }else if(exception instanceof AccessDeniedException){
            ResponseUtils.failed(ExceptionKeys.HTTP_ERROR_403,response);
        }
    }

    @Component
    static class CustomBearerTokenResolver implements BearerTokenResolver{
        private final TokenResolver tokenResolver;
        private final BearerTokenResolver defaultBearerTokenResolver;

        public CustomBearerTokenResolver(ObjectProvider<TokenResolver> beanProvider) {
            this.tokenResolver = beanProvider.getIfUnique();
            this.defaultBearerTokenResolver = new DefaultBearerTokenResolver();
        }

        @Override
        public String resolve(HttpServletRequest request) {
            String tokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (McnUtils.isNotNullAndEmpty(tokenHeader)) {
                return defaultBearerTokenResolver.resolve(request);
            }
            if(tokenResolver != null){
                String name = tokenResolver.paramName();
                String apk = request.getHeader(name);
                if (McnUtils.isNullOrEmpty(apk)) {
                    apk = request.getParameter(name);
                }
                if (McnUtils.isNotNullAndEmpty(apk)) {
                    RestResp<LoginRsp> login = tokenResolver.resolve(apk);
                    if (login.getData() != null) {
                        return login.getData().getToken().substring("Bearer".length()).trim();
                    }
                }
            }
            return null;
        }

    }

}
