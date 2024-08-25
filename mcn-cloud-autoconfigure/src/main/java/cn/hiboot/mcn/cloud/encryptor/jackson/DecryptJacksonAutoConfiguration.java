package cn.hiboot.mcn.cloud.encryptor.jackson;

import cn.hiboot.mcn.cloud.encryptor.sm2.SM2AutoConfiguration;
import cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * DecryptJacksonAutoConfiguration
 *
 * @author DingHao
 * @since 2022/2/17 14:34
 */
@AutoConfiguration(after = SM2AutoConfiguration.class)
@ConditionalOnBean(TextEncryptor.class)
@Import(DecryptJacksonAutoConfiguration.EncryptDecryptBeanPostProcessor.class)
@ConditionalOnClass(ObjectMapper.class)
public class DecryptJacksonAutoConfiguration {

    static class EncryptDecryptBeanPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof ObjectMapper mapper) {
                AnnotationIntrospector primary = mapper.getDeserializationConfig().getAnnotationIntrospector();
                AnnotationIntrospector pair = AnnotationIntrospectorPair.pair(primary, new EncryptDecryptAnnotationIntrospector());
                mapper.setAnnotationIntrospector(pair);
            }
            return bean;
        }

    }

}
