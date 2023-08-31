package cn.hiboot.mcn.cloud.encryptor.web;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.reactive.ReactiveNameValueProcessorFilter;
import cn.hiboot.mcn.autoconfigure.web.filter.common.servlet.NameValueProcessorFilter;
import cn.hiboot.mcn.cloud.encryptor.sm2.SM2AutoConfiguration;
import cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
    @Import(DecryptRequestBodyAdvice.class)
    static class ServletDecryptConverterConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "mcn.decrypt",name = "process-payload",havingValue = "true")
        FilterRegistrationBean<NameValueProcessorFilter> decryptFilterRegistration(DecryptProperties decryptProperties,TextEncryptor textEncryptor) {
            FilterRegistrationBean<NameValueProcessorFilter> filterRegistrationBean = new FilterRegistrationBean<>(new NameValueProcessorFilter(decryptProperties,nameValueProcessor(textEncryptor)));
            filterRegistrationBean.setOrder(decryptProperties.getOrder());
            filterRegistrationBean.setName(decryptProperties.getName());
            return filterRegistrationBean;
        }

        @Configuration(proxyBeanMethods = false)
        static class WebMvcConfig implements WebMvcConfigurer {

            private final TextEncryptor textEncryptor;

            public WebMvcConfig(TextEncryptor textEncryptor) {
                this.textEncryptor = textEncryptor;
            }

            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(new DecryptConverter(textEncryptor,(ConversionService) registry));
            }

        }

    }

    private static NameValueProcessor nameValueProcessor(TextEncryptor textEncryptor){
        return (name, value) -> textEncryptor.decrypt(value);
    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveDecryptConverterConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "mcn.decrypt",name = "process-payload",havingValue = "true")
        ReactiveNameValueProcessorFilter decryptReactiveNameValueProcessorFilter(DecryptProperties decryptProperties, TextEncryptor textEncryptor) {
            return new ReactiveNameValueProcessorFilter(decryptProperties,nameValueProcessor(textEncryptor));
        }

        @Configuration(proxyBeanMethods = false)
        static class WebFluxConfig implements WebFluxConfigurer {

            private final TextEncryptor textEncryptor;

            public WebFluxConfig(TextEncryptor textEncryptor) {
                this.textEncryptor = textEncryptor;
            }

            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(new DecryptConverter(textEncryptor,(ConversionService) registry));
            }

        }

    }
}
