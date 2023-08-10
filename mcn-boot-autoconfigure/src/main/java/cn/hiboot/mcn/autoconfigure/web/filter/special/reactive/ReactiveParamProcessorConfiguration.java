package cn.hiboot.mcn.autoconfigure.web.filter.special.reactive;

import cn.hiboot.mcn.autoconfigure.web.filter.common.reactive.ReactiveNameValueProcessorFilter;
import cn.hiboot.mcn.autoconfigure.web.filter.special.CheckParam;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.reactive.result.method.annotation.ModelAttributeMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * ReactiveParamProcessorConfiguration
 *
 * @author DingHao
 * @since 2023/5/23 12:22
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveParamProcessorConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "param.processor", name = "use-filter", havingValue = "true", matchIfMissing = true)
    public ReactiveNameValueProcessorFilter paramProcessorFilterRegistration(ParamProcessor paramProcessor, ParamProcessorProperties properties) {
        return new ReactiveNameValueProcessorFilter(properties, paramProcessor);
    }

    @Bean
    static WebFluxConfigurer webFluxConfigurer(ParamProcessor paramProcessor,@Lazy @Qualifier("webFluxAdapterRegistry") ReactiveAdapterRegistry reactiveAdapterRegistry) {
        return new WebFluxConfigurer() {
            @Override
            public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
                configurer.addCustomResolver(new KeyValueArgumentResolver(paramProcessor));
                configurer.addCustomResolver(new HandlerMethodArgumentResolver() {

                    private final ModelAttributeMethodArgumentResolver processor = new ModelAttributeMethodArgumentResolver(reactiveAdapterRegistry, true);

                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return processor.supportsParameter(parameter) && (parameter.hasParameterAnnotation(CheckParam.class) || parameter.getParameterType().getAnnotation(CheckParam.class) != null);
                    }

                    @Override
                    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
                        return processor.resolveArgument(parameter, bindingContext, exchange).filter(Objects::nonNull).map(returnValue -> ParamProcessorAutoConfiguration.validStringValue(parameter,returnValue,paramProcessor));
                    }

                });
            }
        };
    }

    static class KeyValueArgumentResolver implements HandlerMethodArgumentResolver {

        private final ParamProcessor paramProcessor;

        public KeyValueArgumentResolver(ParamProcessor paramProcessor) {
            this.paramProcessor = paramProcessor;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CheckParam.class) && parameter.getParameterType() == String.class;
        }

        @Override
        public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
            String name = parameter.getParameterName();
            if (name == null) {
                return Mono.empty();
            }
            String rule = parameter.getParameterAnnotation(CheckParam.class).value();
            return Mono.fromSupplier(() -> paramProcessor.process(rule, name, exchange.getRequest().getQueryParams().getFirst(name)));
        }

    }
}
