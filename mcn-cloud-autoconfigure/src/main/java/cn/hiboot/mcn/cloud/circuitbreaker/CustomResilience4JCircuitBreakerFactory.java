package cn.hiboot.mcn.cloud.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jBulkheadProvider;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * CustomResilience4JCircuitBreakerFactory
 *
 * @author DingHao
 * @since 2025/1/3 14:48
 */
public class CustomResilience4JCircuitBreakerFactory extends
		CircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> {

	private Resilience4jBulkheadProvider bulkheadProvider;

	private Function<String, Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration> defaultConfiguration;

	private CircuitBreakerRegistry circuitBreakerRegistry;

	private TimeLimiterRegistry timeLimiterRegistry;

	private ExecutorService executorService = Executors.newCachedThreadPool();

	private ConcurrentHashMap<String, ExecutorService> executorServices = new ConcurrentHashMap<>();

	private Map<String, Customizer<CircuitBreaker>> circuitBreakerCustomizers = new HashMap<>();

	private Resilience4JConfigurationProperties resilience4JConfigurationProperties;

	public CustomResilience4JCircuitBreakerFactory(CircuitBreakerRegistry circuitBreakerRegistry,
												   TimeLimiterRegistry timeLimiterRegistry, Resilience4jBulkheadProvider bulkheadProvider,
												   Resilience4JConfigurationProperties resilience4JConfigurationProperties) {
		this.circuitBreakerRegistry = circuitBreakerRegistry;
		this.timeLimiterRegistry = timeLimiterRegistry;
		this.bulkheadProvider = bulkheadProvider;
		this.defaultConfiguration = id -> new Resilience4JConfigBuilder(id)
				.circuitBreakerConfig(this.circuitBreakerRegistry.getDefaultConfig())
				.timeLimiterConfig(this.timeLimiterRegistry.getDefaultConfig()).build();
		this.resilience4JConfigurationProperties = resilience4JConfigurationProperties;
	}

	@Override
	protected Resilience4JConfigBuilder configBuilder(String id) {
		return new Resilience4JConfigBuilder(id);
	}

	@Override
	public void configureDefault(
			Function<String, Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration> defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	public void configureCircuitBreakerRegistry(CircuitBreakerRegistry registry) {
		this.circuitBreakerRegistry = registry;
	}

	public CircuitBreakerRegistry getCircuitBreakerRegistry() {
		return this.circuitBreakerRegistry;
	}

	public TimeLimiterRegistry getTimeLimiterRegistry() {
		return this.timeLimiterRegistry;
	}

	public Resilience4jBulkheadProvider getBulkheadProvider() {
		return this.bulkheadProvider;
	}

	public void configureExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public org.springframework.cloud.client.circuitbreaker.CircuitBreaker create(String id) {
		Assert.hasText(id, "A CircuitBreaker must have an id.");
		return create(id, id, this.executorService);
	}

	@Override
	public org.springframework.cloud.client.circuitbreaker.CircuitBreaker create(String id, String groupName) {
		Assert.hasText(id, "A CircuitBreaker must have an id.");
		Assert.hasText(groupName, "A CircuitBreaker must have a group name.");
		final ExecutorService groupExecutorService = executorServices.computeIfAbsent(groupName,
				group -> Executors.newCachedThreadPool());
		return create(id, groupName, groupExecutorService);
	}

	public void addCircuitBreakerCustomizer(Customizer<CircuitBreaker> customizer, String... ids) {
		for (String id : ids) {
			circuitBreakerCustomizers.put(id, customizer);
		}
	}

	private CustomResilience4JCircuitBreaker create(String id, String groupName,
													ExecutorService circuitBreakerExecutorService) {
		Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration config = getConfigurations()
				.computeIfAbsent(id, defaultConfiguration);
		if (resilience4JConfigurationProperties.isDisableThreadPool()) {
			return new CustomResilience4JCircuitBreaker(id, groupName, config.getCircuitBreakerConfig(),
					config.getTimeLimiterConfig(), circuitBreakerRegistry, timeLimiterRegistry,
					Optional.ofNullable(circuitBreakerCustomizers.get(id)), bulkheadProvider);
		}
		else {
			return new CustomResilience4JCircuitBreaker(id, groupName, config.getCircuitBreakerConfig(),
					config.getTimeLimiterConfig(), circuitBreakerRegistry, timeLimiterRegistry,
					circuitBreakerExecutorService, Optional.ofNullable(circuitBreakerCustomizers.get(id)),
					bulkheadProvider);
		}
	}

}
