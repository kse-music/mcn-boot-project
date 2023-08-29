package cn.hiboot.mcn.cloud.security.resource;

import cn.hiboot.mcn.autoconfigure.web.exception.HttpStatusCodeResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.mvc.ResponseUtils;
import cn.hiboot.mcn.autoconfigure.web.reactor.ServerHttpResponseUtils;
import cn.hiboot.mcn.cloud.security.SessionHolder;
import cn.hiboot.mcn.cloud.security.configurer.AuthenticationReload;
import cn.hiboot.mcn.cloud.security.configurer.ReloadAuthenticationConfigurer;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

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

    @Bean
    HttpStatusCodeResolver resourceServerHttpStatusCodeResolver(){
        return exception -> {
            if(exception instanceof AuthenticationException){
                return ExceptionKeys.HTTP_ERROR_401;
            }else if(exception instanceof AccessDeniedException){
                return ExceptionKeys.HTTP_ERROR_403;
            }
            return null;
        };
    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveResourceServerConfiguration {
        private final ExceptionHandler exceptionHandler;

        ReactiveResourceServerConfiguration(ExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }

        @Bean
        @ConditionalOnMissingBean(SecurityWebFilterChain.class)
        SecurityWebFilterChain resourceServerSecurityFilterChain(ServerHttpSecurity http,ResourceServerProperties ssoProperties
                ,ObjectProvider<AuthenticationReload> authenticationReloads,ObjectProvider<TokenResolver> tokenResolvers){
            authenticationReloads.ifUnique(authenticationReload -> http.addFilterBefore(new ReloadAuthenticationWebFilter(authenticationReload), SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION));
            return http
                    .authorizeExchange(requests -> {
                        if (McnUtils.isNotNullAndEmpty(ssoProperties.getAllowedPaths())) {
                            requests.pathMatchers(ssoProperties.getAllowedPaths().toArray(new String[0])).permitAll();
                        }
                        requests.anyExchange().authenticated();
                    })
                    .oauth2ResourceServer(c -> {
                        if(ssoProperties.isOpaqueToken()){
                            c.opaqueToken(Customizer.withDefaults());
                        }else {
                            c.jwt(Customizer.withDefaults());
                        }
                        c.accessDeniedHandler((exchange, accessDeniedException) -> handleException(accessDeniedException, exchange.getResponse()))
                                .authenticationEntryPoint((exchange, authException) -> handleException(authException, exchange.getResponse()));
                        tokenResolvers.ifUnique(tokenResolver -> c.bearerTokenConverter(new ServerBearerTokenAuthenticationConverter(){
                            @Override
                            public Mono<Authentication> convert(ServerWebExchange exchange) {
                                ServerHttpRequest request = exchange.getRequest();
                                String tokenHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                                if (McnUtils.isNotNullAndEmpty(tokenHeader)) {
                                    return super.convert(exchange);
                                }
                                return Mono.just(tokenResolver.paramName()).flatMap(name -> {
                                    String apk = request.getHeaders().getFirst(name);
                                    if (McnUtils.isNullOrEmpty(apk)) {
                                        apk = request.getHeaders().getFirst(name);
                                    }
                                    String token = "";
                                    RestResp<LoginRsp> login = tokenResolver.resolve(apk);
                                    if (login.getData() != null) {
                                        token = login.getData().getToken().substring("Bearer".length()).trim();
                                    }
                                    if (token.isEmpty()) {
                                        return Mono.error(ServiceException.newInstance(name + "不正确"));
                                    }
                                    return Mono.just(new BearerTokenAuthenticationToken(token));
                                });
                            }

                        }));
                    })
                    .build();
        }

        private Mono<Void> handleException(RuntimeException exception, ServerHttpResponse response){
            return ServerHttpResponseUtils.write(exceptionHandler.handleException(exception),response);
        }

        static class ReloadAuthenticationWebFilter implements WebFilter{

            private final AuthenticationReload authenticationReload;

            public ReloadAuthenticationWebFilter(AuthenticationReload authenticationReload) {
                this.authenticationReload = authenticationReload;
            }

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                return ReactiveSecurityContextHolder.getContext().filter(c -> c.getAuthentication() != null)
                        .flatMap(securityContext -> {
                            Authentication authentication = securityContext.getAuthentication();
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
                                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                                }
                            }
                            return Mono.empty();
                        })
                        .switchIfEmpty(chain.filter(exchange).then(Mono.empty())).then();
            }
        }

    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletResourceServerConfiguration {
        private final ExceptionHandler exceptionHandler;

        ServletResourceServerConfiguration(ExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }

        @Bean
        @ConditionalOnDefaultWebSecurity
        SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http,ResourceServerProperties ssoProperties) throws Exception {
            http
                .authorizeHttpRequests(requests -> {
                    if (McnUtils.isNotNullAndEmpty(ssoProperties.getAllowedPaths())) {
                        requests.requestMatchers(ssoProperties.getAllowedPaths().toArray(new String[0])).permitAll();
                    }
                    requests.anyRequest().authenticated();
                })
                .oauth2ResourceServer(c -> {
                    if(ssoProperties.isOpaqueToken()){
                        c.opaqueToken(Customizer.withDefaults());
                    }else {
                        c.jwt(Customizer.withDefaults());
                    }
                    c.accessDeniedHandler((request, response, accessDeniedException) -> handleException(accessDeniedException,response))
                            .authenticationEntryPoint((request, response, authException) -> handleException(authException,response));
                })
                .apply(new ReloadAuthenticationConfigurer());
            return http.build();
        }

        private void handleException(RuntimeException exception, HttpServletResponse response){
            ResponseUtils.write(exceptionHandler.handleException(exception),response);
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

}
