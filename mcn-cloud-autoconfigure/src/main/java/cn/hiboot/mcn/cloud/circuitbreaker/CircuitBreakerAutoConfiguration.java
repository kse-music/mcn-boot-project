package cn.hiboot.mcn.cloud.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JAutoConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jBulkheadProvider;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.context.annotation.Bean;

/**
 * CircuitBreakerAutoConfiguration
 *
 * @author DingHao
 * @since 2024/11/5 16:30
 */
@AutoConfiguration(before = Resilience4JAutoConfiguration.class)
@ConditionalOnClass(Resilience4JCircuitBreakerFactory.class)
@ConditionalOnProperty(prefix = "mcn.circuitbreaker", name = "default.enabled", havingValue = "true", matchIfMissing = true)
public class CircuitBreakerAutoConfiguration {

    @Bean
    Resilience4JCircuitBreakerFactory resilience4jCircuitBreakerFactory(CircuitBreakerRegistry circuitBreakerRegistry, TimeLimiterRegistry timeLimiterRegistry,
                                                                        @Autowired(required = false) Resilience4jBulkheadProvider bulkheadProvider,
                                                                        Resilience4JConfigurationProperties resilience4JConfigurationProperties) {
        return new Resilience4JCircuitBreakerFactory(circuitBreakerRegistry,
                timeLimiterRegistry, bulkheadProvider, resilience4JConfigurationProperties) {
            @Override
            public CircuitBreaker create(String id) {
                return new DefaultCircuitBreaker(super.create(id));
            }

            @Override
            public CircuitBreaker create(String id, String groupName) {
                return new DefaultCircuitBreaker(super.create(id, groupName));
            }
        };
    }

}
