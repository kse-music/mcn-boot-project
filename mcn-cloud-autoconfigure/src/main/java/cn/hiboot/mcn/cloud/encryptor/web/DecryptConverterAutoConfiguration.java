package cn.hiboot.mcn.cloud.encryptor.web;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.reactive.ReactiveNameValueProcessorFilter;
import cn.hiboot.mcn.autoconfigure.web.filter.common.servlet.NameValueProcessorFilter;
import cn.hiboot.mcn.cloud.encryptor.Decrypt;
import cn.hiboot.mcn.cloud.encryptor.sm2.SM2AutoConfiguration;
import cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor;
import cn.hiboot.mcn.core.util.McnUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * DecryptConverterAutoConfiguration
 *
 * @author DingHao
 * @since 2022/2/17 11:47
 */
@AutoConfiguration(after = SM2AutoConfiguration.class)
@ConditionalOnBean(TextEncryptor.class)
@EnableConfigurationProperties(DecryptProperties.class)
public class DecryptConverterAutoConfiguration {

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @Import({DecryptRequestBodyAdvice.class, ServletDecryptConverterConfiguration.WebMvcConfig.class})
    static class ServletDecryptConverterConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "mcn.decrypt",name = "process-payload",havingValue = "true")
        FilterRegistrationBean<NameValueProcessorFilter> decryptFilterRegistration(DecryptProperties decryptProperties,TextEncryptor textEncryptor) {
            FilterRegistrationBean<NameValueProcessorFilter> filterRegistrationBean = new FilterRegistrationBean<>(new NameValueProcessorFilter(decryptProperties,nameValueProcessor(textEncryptor)));
            filterRegistrationBean.setOrder(decryptProperties.getOrder());
            filterRegistrationBean.setName(decryptProperties.getName());
            return filterRegistrationBean;
        }

        static class WebMvcConfig implements WebMvcConfigurer {

            private final TextEncryptor textEncryptor;

            public WebMvcConfig(TextEncryptor textEncryptor) {
                this.textEncryptor = textEncryptor;
            }

            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(new DecryptConverter(textEncryptor,(ConversionService) registry));
            }

            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(Decrypt.class) && parameter.getParameterType() == String.class;
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
                        if(request == null){
                            return null;
                        }
                        String value = request.getParameter(parameter.getParameterName());
                        if(McnUtils.isNullOrEmpty(value)){
                            return null;
                        }
                        return textEncryptor.decrypt(value);
                    }
                });
            }

        }

    }

    private static NameValueProcessor nameValueProcessor(TextEncryptor textEncryptor){
        return (name, value) -> textEncryptor.decrypt(value);
    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @Import(ReactiveDecryptConverterConfiguration.WebFluxConfig.class)
    static class ReactiveDecryptConverterConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "mcn.decrypt",name = "process-payload",havingValue = "true")
        ReactiveNameValueProcessorFilter decryptReactiveNameValueProcessorFilter(DecryptProperties decryptProperties, TextEncryptor textEncryptor) {
            return new ReactiveNameValueProcessorFilter(decryptProperties,nameValueProcessor(textEncryptor));
        }

        static class WebFluxConfig implements WebFluxConfigurer {

            private final TextEncryptor textEncryptor;

            public WebFluxConfig(TextEncryptor textEncryptor) {
                this.textEncryptor = textEncryptor;
            }

            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(new DecryptConverter(textEncryptor,(ConversionService) registry));
            }

            @Override
            public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
                configurer.addCustomResolver(new org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver() {

                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(Decrypt.class) && parameter.getParameterType() == String.class;
                    }

                    @Override
                    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
                        return Mono.fromSupplier(() -> exchange.getRequest().getQueryParams().getFirst(parameter.getParameterName()))
                                .switchIfEmpty(Mono.empty()).map(textEncryptor::decrypt);
                    }
                });
            }

        }

    }
}
