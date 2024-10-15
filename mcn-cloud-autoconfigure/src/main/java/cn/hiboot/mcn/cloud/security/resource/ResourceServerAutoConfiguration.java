package cn.hiboot.mcn.cloud.security.resource;

import cn.hiboot.mcn.autoconfigure.web.exception.HttpStatusCodeResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.reactor.WebUtils;
import cn.hiboot.mcn.cloud.security.configurer.AuthenticationReload;
import cn.hiboot.mcn.cloud.security.configurer.ReloadAuthenticationConfigurer;
import cn.hiboot.mcn.cloud.security.token.ServerTokenResolver;
import cn.hiboot.mcn.cloud.security.token.TokenResolver;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CsrfWebFilter;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * ResourceServerAutoConfiguration
 *
 * @author DingHao
 * @since 2023/2/8 15:26
 */
@AutoConfiguration(before = {OAuth2ResourceServerAutoConfiguration.class, ReactiveOAuth2ResourceServerAutoConfiguration.class})
@EnableConfigurationProperties(ResourceServerProperties.class)
@ConditionalOnClass(BearerTokenAuthenticationToken.class)
@ConditionalOnProperty(prefix = "sso", name = "enabled", havingValue = "true", matchIfMissing = true)
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

    @ConditionalOnClass(EnableWebFluxSecurity.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnMissingBean(SecurityWebFilterChain.class)
    static class ReactiveResourceServerConfiguration {
        private final ExceptionHandler exceptionHandler;

        ReactiveResourceServerConfiguration(ExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }

        @Bean
        SecurityWebFilterChain resourceServerSecurityFilterChain(ServerHttpSecurity http,ResourceServerProperties ssoProperties
                ,ObjectProvider<AuthenticationReload> authenticationReloads,ObjectProvider<ServerTokenResolver> apkResolvers){
            authenticationReloads.ifUnique(authenticationReload ->
                    http.addFilterBefore((exchange, chain) ->
                            ReactiveSecurityContextHolder.getContext().doOnNext(securityContext -> ReloadAuthenticationConfigurer.reloadAuthentication(securityContext, authenticationReload)).then(chain.filter(exchange)),
                            SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION));
            return http
                    .authorizeExchange(requests -> {
                        if(McnUtils.isNotNullAndEmpty(ssoProperties.getAllowedPaths())){
                            requests.pathMatchers(ssoProperties.getAllowedPaths()).permitAll();
                        }
                        requests.anyExchange().authenticated();
                    })
                    .csrf(c -> c.requireCsrfProtectionMatcher(registerDefaultCsrfOverride(ssoProperties.getAllowedPaths())))
                    .oauth2ResourceServer(c -> {
                        if(ssoProperties.isOpaqueToken()){
                            c.opaqueToken();
                        }else {
                            c.jwt();
                        }
                        c.accessDeniedHandler((exchange, accessDeniedException) -> handleException(accessDeniedException, exchange.getResponse()))
                                .authenticationEntryPoint((exchange, authException) -> handleException(authException, exchange.getResponse()));
                        apkResolvers.ifUnique(apkResolver -> c.bearerTokenConverter(new ServerBearerTokenAuthenticationConverter(){
                            @Override
                            public Mono<Authentication> convert(ServerWebExchange exchange) {
                                return super.convert(exchange).switchIfEmpty(jwtToken(apkResolver,exchange.getRequest()));
                            }
                        }));
                    })
                    .build();
        }

        private ServerWebExchangeMatcher registerDefaultCsrfOverride(String[] ignorePath) {
            if (ignorePath.length == 0) {
                return CsrfWebFilter.DEFAULT_CSRF_MATCHER;
            }
            return new AndServerWebExchangeMatcher(CsrfWebFilter.DEFAULT_CSRF_MATCHER,
                    new NegatedServerWebExchangeMatcher(new OrServerWebExchangeMatcher(Arrays.stream(ignorePath).map(PathPatternParserServerWebExchangeMatcher::new).collect(Collectors.toList()))));
        }

        private Mono<Authentication> jwtToken(ServerTokenResolver apkResolver,ServerHttpRequest request){
            return Mono.fromCallable(() -> {
                String name = apkResolver.paramName();
                String apk = request.getHeaders().getFirst(name);
                if (McnUtils.isNullOrEmpty(apk)) {
                    apk = request.getQueryParams().getFirst(name);
                }
                return apk;
            }).flatMap(apkResolver::jwtToken);
        }

        private Mono<Void> handleException(RuntimeException exception, ServerHttpResponse response){
            return WebUtils.write(exceptionHandler.handleException(exception),response);
        }

    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnDefaultWebSecurity
    static class ServletResourceServerConfiguration {

        private final ExceptionHandler exceptionHandler;

        ServletResourceServerConfiguration(ExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }

        @Bean
        SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http,ResourceServerProperties ssoProperties,ObjectProvider<TokenResolver> beanProvider) throws Exception {
            return http
                    .authorizeRequests(requests -> {
                        requests.antMatchers(ssoProperties.getAllowedPaths()).permitAll();
                        requests.anyRequest().authenticated();
                    })
                    .csrf(c -> c.ignoringAntMatchers(ssoProperties.getAllowedPaths()))
                    .oauth2ResourceServer(c -> {
                        if(ssoProperties.isOpaqueToken()){
                            c.opaqueToken();
                        }else {
                            c.jwt();
                        }
                        c.bearerTokenResolver(new CustomBearerTokenResolver(beanProvider)).accessDeniedHandler((request, response, accessDeniedException) -> handleException(accessDeniedException,response))
                                .authenticationEntryPoint((request, response, authException) -> handleException(authException,response));
                    })
                    .apply(new ReloadAuthenticationConfigurer()).and()
                    .build();
        }

        private void handleException(RuntimeException exception, HttpServletResponse response){
            cn.hiboot.mcn.autoconfigure.web.mvc.WebUtils.write(exceptionHandler.handleException(exception),response);
        }

        static class CustomBearerTokenResolver implements BearerTokenResolver{
            private final TokenResolver tokenResolver;
            private final BearerTokenResolver defaultBearerTokenResolver;

            public CustomBearerTokenResolver(ObjectProvider<TokenResolver> beanProvider) {
                this.tokenResolver = beanProvider.getIfUnique();
                this.defaultBearerTokenResolver = new DefaultBearerTokenResolver();
            }

            @Override
            public String resolve(HttpServletRequest request) {
                String token = defaultBearerTokenResolver.resolve(request);
                if (token == null && tokenResolver != null) {
                    String name = tokenResolver.paramName();
                    String apk = request.getHeader(name);
                    if (McnUtils.isNullOrEmpty(apk)) {
                        apk = request.getParameter(name);
                    }
                    if (McnUtils.isNotNullAndEmpty(apk)) {
                        return tokenResolver.jwtToken(apk);
                    }
                }
                return token;
            }

        }

    }

}
