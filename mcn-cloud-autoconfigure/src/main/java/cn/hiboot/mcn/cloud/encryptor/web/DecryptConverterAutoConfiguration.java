package cn.hiboot.mcn.cloud.encryptor.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(DecryptRequestBodyAdvice.class)
public class DecryptConverterAutoConfiguration {

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
