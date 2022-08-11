package cn.hiboot.mcn.cloud.encryptor.jackson;

import cn.hiboot.mcn.cloud.encryptor.sm2.SM2AutoConfiguration;
import cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * DecryptJacksonAutoConfiguration
 *
 * @author DingHao
 * @since 2022/2/17 14:34
 */
@AutoConfiguration(after = SM2AutoConfiguration.class)
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
