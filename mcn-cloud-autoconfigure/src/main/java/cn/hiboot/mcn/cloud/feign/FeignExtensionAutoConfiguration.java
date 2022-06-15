package cn.hiboot.mcn.cloud.feign;

import cn.hiboot.mcn.cloud.security.SessionHolder;
import feign.*;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.openfeign.Targeter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * 提供全局fallback机制
 *
 * @author DingHao
 * @since 2021/9/21 13:37
 */
@AutoConfiguration
@ConditionalOnClass(Feign.class)
public class FeignExtensionAutoConfiguration {


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Resilience4JCircuitBreakerFactory.class)
    @Conditional(GlobalFallbackCondition.class)
    protected static class CircuitBreakerConfiguration {

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

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({DefaultAuthenticationEventPublisher.class,JwtAuthenticationToken.class})
    protected static class FeignRequestInterceptor implements RequestInterceptor {

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
    protected static class FeignErrorDecoder implements ErrorDecoder {

        @Override
        public Exception decode(String methodKey, Response response) {
            return FeignException.errorStatus(methodKey, response);
        }

    }

    static class GlobalFallbackCondition extends AllNestedConditions {

        GlobalFallbackCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "feign.circuitbreaker",name = "enabled",havingValue = "true")
        static class FeignCircuitbreakerEnabled {

        }

        @ConditionalOnProperty(prefix = "feign.circuitbreaker",name = "globalfallback.enabled", havingValue = "true",matchIfMissing = true)
        static class FeignGlobalCircuitbreakerEnabled {

        }

    }

}
