package cn.hiboot.mcn.cloud.feign;

import cn.hiboot.mcn.cloud.security.SessionHolder;
import feign.*;
import feign.codec.ErrorDecoder;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.openfeign.Targeter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.concurrent.TimeUnit;

/**
 * feign和断路器自动配置
 * 提供全局fallback机制
 *
 * @author DingHao
 * @since 2021/9/21 13:37
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Feign.class)
@EnableConfigurationProperties(FeignCircuitBreakerProperties.class)
public class FeignCircuitBreakerAutoConfiguration {

    private final FeignCircuitBreakerProperties properties;

    public FeignCircuitBreakerAutoConfiguration(FeignCircuitBreakerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return properties.getLevel();
    }

    @Bean
    public Request.Options options(){
        return new Request.Options(properties.getConnectTimeout(), TimeUnit.SECONDS, properties.getReadTimeout(), TimeUnit.SECONDS, properties.isFollowRedirects());
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Resilience4JCircuitBreakerFactory.class)
    @Conditional(BreakerCondition.class)
    private static class CircuitBreakerConfiguration {

        private final FeignCircuitBreakerProperties properties;

        public CircuitBreakerConfiguration(FeignCircuitBreakerProperties properties) {
            this.properties = properties;
        }

        @Bean
        public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
            return factory -> factory.configureDefault(
                    id -> new Resilience4JConfigBuilder(id)
                            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(properties.getTimeoutDuration()).cancelRunningFuture(properties.isCancelRunningFuture()).build())
                            .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                            .build());
        }

        @ConditionalOnProperty(prefix = "feign.circuitbreaker",name = "globalfallback.enabled", havingValue = "true",matchIfMissing = true)
        private static class GlobalFallbackConfig{

            @Bean
            @Scope("prototype")
            public Feign.Builder circuitBreakerFeignBuilder() {
                return FeignCircuitBreaker.builder();
            }

            @Bean
            public Targeter circuitBreakerFeignTargeter(CircuitBreakerFactory circuitBreakerFactory,
                                                        @Value("${feign.circuitbreaker.group.enabled:false}") boolean circuitBreakerGroupEnabled) {
                return new FeignCircuitBreakerTargeter(circuitBreakerFactory, circuitBreakerGroupEnabled);
            }

        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({DefaultAuthenticationEventPublisher.class,JwtAuthenticationToken.class})
    private static class FeignRequestInterceptor implements RequestInterceptor {

        private static final String TOKEN_TYPE = "Bearer ";
        private static final String AUTHORIZATION = "Authorization";

        @Override
        public void apply(RequestTemplate requestTemplate) {
            String authorization = SessionHolder.getToken();
            if (authorization != null) {
                requestTemplate.header(AUTHORIZATION, TOKEN_TYPE.concat(authorization));
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    private static class FeignErrorDecoder implements ErrorDecoder {

        @Override
        public Exception decode(String methodKey, Response response) {
            return FeignException.errorStatus(methodKey, response);
        }

    }

   static class BreakerCondition extends AnyNestedCondition {

        BreakerCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "feign.circuitbreaker",name = "enabled",havingValue = "true")
        static class NoComponentsAvailable {

        }

        @ConditionalOnProperty(prefix = "feign.circuitbreaker",name = "globalfallback.enabled", havingValue = "true")
        static class CookieHttpSessionIdResolverAvailable {

        }

    }

}
