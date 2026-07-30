package cn.hiboot.mcn.cloud.encryptor.jackson;

import cn.hiboot.mcn.cloud.encryptor.sm2.SM2AutoConfiguration;
import cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.introspect.AnnotationIntrospectorPair;

/**
 * DecryptJacksonAutoConfiguration
 *
 * @author DingHao
 * @since 2022/2/17 14:34
 */
@AutoConfiguration(after = SM2AutoConfiguration.class)
@ConditionalOnBean(TextEncryptor.class)
@ConditionalOnClass(name = "tools.jackson.databind.ObjectMapper")
public class DecryptJacksonAutoConfiguration {

    @Bean
    JsonMapperBuilderCustomizer encryptDecryptJsonMapperBuilderCustomizer() {
        return builder -> {
            AnnotationIntrospector primary = builder.annotationIntrospector();
            AnnotationIntrospector pair = AnnotationIntrospectorPair.pair(primary, new EncryptDecryptAnnotationIntrospector());
            builder.annotationIntrospector(pair);
        };
    }

}
