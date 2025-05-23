package cn.hiboot.mcn.autoconfigure.web.security;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.handler.DefaultWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSecurityAutoConfiguration
 * 配置忽略的请求路径
 *
 * @author DingHao
 * @since 2021/5/23 23:36
 */
@AutoConfiguration
@ConditionalOnClass(WebSecurity.class)
@EnableConfigurationProperties(WebSecurityProperties.class)
public class WebSecurityAutoConfiguration {

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @Configuration(proxyBeanMethods = false)
    static class ServletWebSecurityConfiguration {

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer(WebSecurityProperties webSecurityProperties) {
            return web -> {
                List<String> urls = ignoreUrl(webSecurityProperties);
                if (McnUtils.isNotNullAndEmpty(urls)) {
                    web.ignoring().requestMatchers(new OrRequestMatcher(urls.stream().map(url -> PathPatternRequestMatcher.withDefaults().matcher(url))
                            .collect(Collectors.toList())));
                }
            };
        }
    }

    private static List<String> ignoreUrl(WebSecurityProperties webSecurityProperties) {
        List<String> urls = new ArrayList<>();
        if (webSecurityProperties.isEnableDefaultIgnore()) {
            Collections.addAll(urls, webSecurityProperties.getDefaultExcludeUrls());
        }
        if (webSecurityProperties.getExcludeUrls() != null) {
            Collections.addAll(urls, webSecurityProperties.getExcludeUrls());
        }
        return urls;
    }

    @Bean
    public ExceptionResolver<AccessDeniedException> securityExceptionResolver() {
        return t -> RestResp.error(ExceptionKeys.HTTP_ERROR_403);
    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @Configuration(proxyBeanMethods = false)
    @Import(ReactiveWebSecurityConfiguration.IgnoreUrlFilter.class)
    static class ReactiveWebSecurityConfiguration {

        @Order(-101)
        static class IgnoreUrlFilter implements WebFilter {

            private final ServerWebExchangeMatcher requiresAuthenticationMatcher;

            public IgnoreUrlFilter(WebSecurityProperties webSecurityProperties) {
                this.requiresAuthenticationMatcher = ServerWebExchangeMatchers.pathMatchers(ignoreUrl(webSecurityProperties).toArray(new String[0]));
            }

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                return Mono.just(chain).cast(DefaultWebFilterChain.class)
                        .filterWhen(c -> requiresAuthenticationMatcher.matches(exchange).map(ServerWebExchangeMatcher.MatchResult::isMatch))
                        .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                        .flatMap(c -> c.getHandler().handle(exchange));
            }

        }

    }

}
