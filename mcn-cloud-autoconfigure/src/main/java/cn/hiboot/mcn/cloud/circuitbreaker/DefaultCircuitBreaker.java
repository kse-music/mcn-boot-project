package cn.hiboot.mcn.cloud.circuitbreaker;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * DefaultCircuitBreaker
 *
 * @author DingHao
 * @since 2024/11/5 16:12
 */
class DefaultCircuitBreaker implements CircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(DefaultCircuitBreaker.class);

    private final CircuitBreaker circuitBreaker;

    DefaultCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
        try {
            return circuitBreaker.run(toRun, fallback);
        } catch (NoFallbackAvailableException e) {
            Throwable cause = e.getCause();
            String errorMessage = cause.getMessage();
            if (cause instanceof FeignException exception && !exception.contentUTF8().isEmpty()) {
                errorMessage = exception.contentUTF8();
            }
            log.error("Using DefaultFallback handle exception", cause);
            return (T) RestResp.error(ExceptionKeys.REMOTE_SERVICE_ERROR, errorMessage);
        }
    }

}