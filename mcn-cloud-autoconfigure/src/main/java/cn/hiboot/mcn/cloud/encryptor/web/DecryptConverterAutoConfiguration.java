package cn.hiboot.mcn.cloud.encryptor.web;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorFilter;
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
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * DecryptConverterAutoConfiguration
 *
 * @author DingHao
 * @since 2022/2/17 11:47
 */
@AutoConfiguration
@ConditionalOnBean(TextEncryptor.class)
@EnableConfigurationProperties(DecryptProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(DecryptRequestBodyAdvice.class)
public class DecryptConverterAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "mcn.decrypt",name = "process-payload",havingValue = "true")
    FilterRegistrationBean<NameValueProcessorFilter> xssFilterRegistration(DecryptProperties decryptProperties,TextEncryptor textEncryptor) {
        FilterRegistrationBean<NameValueProcessorFilter> filterRegistrationBean = new FilterRegistrationBean<>(new NameValueProcessorFilter(decryptProperties,nameValueProcessor(decryptProperties,textEncryptor)));
        filterRegistrationBean.setOrder(decryptProperties.getOrder());
        filterRegistrationBean.setName(decryptProperties.getName());
        return filterRegistrationBean;
    }

   private NameValueProcessor nameValueProcessor(DecryptProperties decryptProperties,TextEncryptor textEncryptor){
        return (name, value) -> {
            try {
                return textEncryptor.decrypt(value);
            }catch (Exception e){
                if(!decryptProperties.isContinueOnError()){
                    throw e;
                }
            }
            return value;
        };
    }

    @Configuration(proxyBeanMethods = false)
    private static class WebMvcConfig implements WebMvcConfigurer {

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
