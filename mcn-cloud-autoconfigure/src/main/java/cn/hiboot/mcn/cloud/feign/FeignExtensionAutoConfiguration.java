package cn.hiboot.mcn.cloud.feign;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorJacksonConfig;
import feign.Feign;
import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.optionals.OptionalDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.openfeign.Targeter;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.*;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 提供全局fallback机制
 *
 * @author DingHao
 * @since 2021/9/21 13:37
 */
@AutoConfiguration
@ConditionalOnClass(Feign.class)
@Import(FeignInterceptorConfiguration.class)
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
    @ConditionalOnClass(ErrorDecoder.class)
    @ConditionalOnMissingBean(ErrorDecoder.class)
    protected static class FeignErrorDecoder implements ErrorDecoder {

        @Override
        public Exception decode(String methodKey, Response response) {
            return FeignException.errorStatus(methodKey, response);
        }

    }


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "param.processor",name = "enable",havingValue = "true")
    public Decoder feignDecoder(ObjectProvider<HttpMessageConverterCustomizer> customizers, ObjectFactory<HttpMessageConverters> messageConverters) {
        return new OptionalDecoder(new ResponseEntityDecoder(new FeignClientResponseInterceptor(messageConverters, customizers)));
    }

    static class FeignClientResponseInterceptor extends SpringDecoder {

        public FeignClientResponseInterceptor(ObjectFactory<HttpMessageConverters> messageConverters,ObjectProvider<HttpMessageConverterCustomizer> customizers) {
            super(messageConverters, customizers);
        }

        @Override
        public Object decode(final Response response, Type type) throws IOException, FeignException {
            try{
                return super.decode(response,type);
            } finally {
                NameValueProcessorJacksonConfig.removeFeignRequest();
            }
        }

    }

    static class GlobalFallbackCondition extends AllNestedConditions {

        GlobalFallbackCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(prefix = "feign.circuitbreaker",name = "enabled",havingValue = "true")
        static class FeignCircuitbreakerEnabled {

        }

        @ConditionalOnProperty(prefix = "feign.circuitbreaker",name = "globalfallback.enabled", havingValue = "true", matchIfMissing = true)
        static class FeignGlobalCircuitbreakerEnabled {

        }

    }

}
