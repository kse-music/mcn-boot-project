package cn.hiboot.mcn.cloud.encryptor.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * DecryptJacksonAutoConfiguration
 *
 * @author DingHao
 * @since 2022/2/17 14:34
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(TextEncryptor.class)
@Import(DecryptJacksonAutoConfiguration.JacksonBuilderCustomizer.class)
@ConditionalOnClass(ObjectMapper.class)
public class DecryptJacksonAutoConfiguration {

    @ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
    static class JacksonBuilderCustomizer implements Jackson2ObjectMapperBuilderCustomizer {

        @Override
        public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
            jacksonObjectMapperBuilder.annotationIntrospector(new EncryptDecryptAnnotationIntrospector());
        }

    }

}
