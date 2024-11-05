package cn.hiboot.mcn.cloud.circuitbreaker;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jBulkheadProvider;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * CustomResilience4JCircuitBreaker
 *
 * @author DingHao
 * @since 2025/1/3 14:48
 */
public class CustomResilience4JCircuitBreaker implements CircuitBreaker {
	private static final Logger log = LoggerFactory.getLogger(CustomResilience4JCircuitBreaker.class);

	static final String CIRCUIT_BREAKER_GROUP_TAG = "group";

	private final String id;

	private final String groupName;

	private Resilience4jBulkheadProvider bulkheadProvider;

	private final io.github.resilience4j.circuitbreaker.CircuitBreakerConfig circuitBreakerConfig;

	private final CircuitBreakerRegistry registry;

	private final TimeLimiterRegistry timeLimiterRegistry;

	private final TimeLimiterConfig timeLimiterConfig;

	private final ExecutorService executorService;

	private final Optional<Customizer<io.github.resilience4j.circuitbreaker.CircuitBreaker>> circuitBreakerCustomizer;

	public CustomResilience4JCircuitBreaker(String id, String groupName,
											io.github.resilience4j.circuitbreaker.CircuitBreakerConfig circuitBreakerConfig,
											TimeLimiterConfig timeLimiterConfig, CircuitBreakerRegistry circuitBreakerRegistry,
											TimeLimiterRegistry timeLimiterRegistry, ExecutorService executorService,
											Optional<Customizer<io.github.resilience4j.circuitbreaker.CircuitBreaker>> circuitBreakerCustomizer,
											Resilience4jBulkheadProvider bulkheadProvider) {
		this.id = id;
		this.groupName = groupName;
		this.circuitBreakerConfig = circuitBreakerConfig;
		this.registry = circuitBreakerRegistry;
		this.timeLimiterRegistry = timeLimiterRegistry;
		this.timeLimiterConfig = timeLimiterConfig;
		this.executorService = executorService;
		this.circuitBreakerCustomizer = circuitBreakerCustomizer;
		this.bulkheadProvider = bulkheadProvider;
	}

	public CustomResilience4JCircuitBreaker(String id, String groupName,
											io.github.resilience4j.circuitbreaker.CircuitBreakerConfig circuitBreakerConfig,
											TimeLimiterConfig timeLimiterConfig, CircuitBreakerRegistry circuitBreakerRegistry,
											TimeLimiterRegistry timeLimiterRegistry,
											Optional<Customizer<io.github.resilience4j.circuitbreaker.CircuitBreaker>> circuitBreakerCustomizer,
											Resilience4jBulkheadProvider bulkheadProvider) {
		this(id, groupName, circuitBreakerConfig, timeLimiterConfig, circuitBreakerRegistry, timeLimiterRegistry, null,
				circuitBreakerCustomizer, bulkheadProvider);
	}

	@Override
	public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
		try {
			return doRun(toRun, fallback);
		} catch (NoFallbackAvailableException e) {
			Throwable cause = e.getCause();
			String errorMessage = cause.getMessage();
			if (cause instanceof FeignException && !((FeignException)cause).contentUTF8().isEmpty()) {
				errorMessage = ((FeignException)cause).contentUTF8();
			}
			log.error("Using DefaultFallback handle exception", cause);
			return (T) RestResp.error(ExceptionKeys.REMOTE_SERVICE_ERROR, errorMessage);
		}
	}

	private <T> T doRun(Supplier<T> toRun, Function<Throwable, T> fallback) {
		final io.vavr.collection.Map<String, String> tags = io.vavr.collection.HashMap.of(CIRCUIT_BREAKER_GROUP_TAG,
				this.groupName);

		io.github.resilience4j.circuitbreaker.CircuitBreaker defaultCircuitBreaker = registry.circuitBreaker(this.id,
				this.circuitBreakerConfig, tags);
		circuitBreakerCustomizer.ifPresent(customizer -> customizer.customize(defaultCircuitBreaker));
		TimeLimiter timeLimiter = loadTimeLimiter().orElseGet(() -> timeLimiterRegistry.timeLimiter(id, timeLimiterConfig, tags));
		if (bulkheadProvider != null) {
			return bulkheadProvider.run(this.groupName, toRun, fallback, defaultCircuitBreaker, timeLimiter, tags);
		}
		else {
			if (executorService != null) {
				Supplier<Future<T>> futureSupplier = () -> executorService.submit(toRun::get);
				Callable restrictedCall = TimeLimiter.decorateFutureSupplier(timeLimiter, futureSupplier);
				Callable<T> callable = io.github.resilience4j.circuitbreaker.CircuitBreaker
						.decorateCallable(defaultCircuitBreaker, restrictedCall);
				return Try.of(callable::call).recover(fallback).get();
			}
			else {
				Supplier<T> decorator = io.github.resilience4j.circuitbreaker.CircuitBreaker
						.decorateSupplier(defaultCircuitBreaker, toRun);
				return Try.of(decorator::get).recover(fallback).get();
			}

		}
	}

	private Optional<TimeLimiter> loadTimeLimiter() {
		return Optional.ofNullable(this.timeLimiterRegistry.find(this.id)
				.orElseGet(() -> this.timeLimiterRegistry.find(this.groupName).orElse(null)));
	}


}
