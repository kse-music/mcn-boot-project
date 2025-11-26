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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.DelegatingServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.csrf.CsrfWebFilter;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    HttpStatusCodeResolver resourceServerHttpStatusCodeResolver() {
        return exception -> {
            if (exception instanceof AuthenticationException) {
                return ExceptionKeys.HTTP_ERROR_401;
            } else if (exception instanceof AccessDeniedException) {
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
        SecurityWebFilterChain resourceServerSecurityFilterChain(ServerHttpSecurity http,
                                                                 ResourceServerProperties ssoProperties,
                                                                 ObjectProvider<ReactiveJwtDecoder> reactiveJwtDecoders,
                                                                 ObjectProvider<AuthenticationReload> authenticationReloads,
                                                                 ObjectProvider<ServerTokenResolver> apkResolvers) {
            authenticationReloads.ifUnique(authenticationReload ->
                    http.addFilterBefore((exchange, chain) ->
                                    ReactiveSecurityContextHolder.getContext().doOnNext(securityContext -> ReloadAuthenticationConfigurer.reloadAuthentication(securityContext, authenticationReload)).then(chain.filter(exchange)),
                            SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION));
            return http
                    .authorizeExchange(requests -> {
                        if (McnUtils.isNotNullAndEmpty(ssoProperties.getAllowedPaths())) {
                            requests.pathMatchers(ssoProperties.getAllowedPaths()).permitAll();
                        }
                        requests.anyExchange().authenticated();
                    })
                    .csrf(c -> c.requireCsrfProtectionMatcher(registerDefaultCsrfOverride(ssoProperties.getAllowedPaths())))
                    .oauth2ResourceServer(c -> {
                        if (ssoProperties.isOpaqueToken()) {
                            c.opaqueToken(Customizer.withDefaults());
                        } else {
                            if (!ssoProperties.isVerifyJwt()) {
                                reactiveJwtDecoders.ifUnique(r -> {
                                    if (r instanceof NimbusReactiveJwtDecoder reactiveJwtDecoder) {
                                        reactiveJwtDecoder.setJwtValidator(token -> OAuth2TokenValidatorResult.success());
                                    }
                                });
                            }
                            c.jwt(Customizer.withDefaults());
                        }
                        c.accessDeniedHandler((exchange, accessDeniedException) -> handleException(accessDeniedException, exchange.getResponse()))
                                .authenticationEntryPoint((exchange, authException) -> handleException(authException, exchange.getResponse()));
                        List<ServerAuthenticationConverter> authenticationConverters = new ArrayList<>(2);
                        authenticationConverters.add(new ServerBearerTokenAuthenticationConverter());
                        apkResolvers.ifUnique(apkResolver -> authenticationConverters.add(exchange -> jwtToken(apkResolver, exchange.getRequest())));
                        c.bearerTokenConverter(new DelegatingServerAuthenticationConverter(authenticationConverters));
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

        private Mono<Authentication> jwtToken(ServerTokenResolver apkResolver, ServerHttpRequest request) {
            return Mono.fromCallable(() -> {
                String name = apkResolver.paramName();
                String apk = request.getHeaders().getFirst(name);
                if (McnUtils.isNullOrEmpty(apk)) {
                    apk = request.getQueryParams().getFirst(name);
                }
                return apk;
            }).flatMap(apkResolver::jwtToken);
        }

        private Mono<Void> handleException(RuntimeException exception, ServerHttpResponse response) {
            return WebUtils.write(exceptionHandler.handleException(exception), response);
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
        SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http, ResourceServerProperties ssoProperties, ObjectProvider<JwtDecoder> jwtDecoders) throws Exception {
            return http
                    .authorizeHttpRequests(requests -> {
                        requests.requestMatchers(ssoProperties.getAllowedPaths()).permitAll();
                        requests.anyRequest().authenticated();
                    })
                    .csrf(c -> c.ignoringRequestMatchers(ssoProperties.getAllowedPaths()))
                    .oauth2ResourceServer(c -> {
                        if (ssoProperties.isOpaqueToken()) {
                            c.opaqueToken(Customizer.withDefaults());
                        } else {
                            if (!ssoProperties.isVerifyJwt()) {
                                jwtDecoders.ifUnique(r -> {
                                    if (r instanceof NimbusJwtDecoder jwtDecoder) {
                                        jwtDecoder.setJwtValidator(token -> OAuth2TokenValidatorResult.success());
                                    }
                                });
                            }
                            c.jwt(Customizer.withDefaults());
                        }
                        c.accessDeniedHandler((request, response, accessDeniedException) -> handleException(accessDeniedException, response))
                                .authenticationEntryPoint((request, response, authException) -> handleException(authException, response));
                    })
                    .with(new ReloadAuthenticationConfigurer(), Customizer.withDefaults())
                    .build();
        }

        private void handleException(RuntimeException exception, HttpServletResponse response) {
            cn.hiboot.mcn.autoconfigure.web.mvc.WebUtils.write(exceptionHandler.handleException(exception), response);
        }

        @Bean
        @ConditionalOnMissingBean
        BearerTokenResolver defaultBearerTokenResolver(ObjectProvider<TokenResolver> beanProvider) {
            return new DelegatingBearerTokenResolver(beanProvider);
        }

        static class DelegatingBearerTokenResolver implements BearerTokenResolver {

            private final List<BearerTokenResolver> delegates;

            public DelegatingBearerTokenResolver(ObjectProvider<TokenResolver> beanProvider) {
                this.delegates = new ArrayList<>(2);
                this.delegates.add(new DefaultBearerTokenResolver());
                beanProvider.ifUnique(tokenResolver -> this.delegates.add(request -> {
                    String name = tokenResolver.paramName();
                    String apk = request.getHeader(name);
                    if (McnUtils.isNullOrEmpty(apk)) {
                        apk = request.getParameter(name);
                    }
                    if (McnUtils.isNotNullAndEmpty(apk)) {
                        return tokenResolver.jwtToken(apk);
                    }
                    return null;
                }));
            }

            @Override
            public String resolve(HttpServletRequest request) {
                for (BearerTokenResolver delegate : this.delegates) {
                    String token = delegate.resolve(request);
                    if (token != null) {
                        return token;
                    }
                }
                return null;
            }
        }

    }

}
