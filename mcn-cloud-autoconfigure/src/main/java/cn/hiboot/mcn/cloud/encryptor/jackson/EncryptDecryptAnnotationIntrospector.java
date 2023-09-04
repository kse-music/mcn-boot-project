package cn.hiboot.mcn.cloud.encryptor.jackson;

import cn.hiboot.mcn.cloud.encryptor.Decrypt;
import cn.hiboot.mcn.cloud.encryptor.Encrypt;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * 在json序列化和反序列化时加解密被注解修饰的数据
 *
 * @author DingHao
 * @since 2022/2/16 18:08
 */
public class EncryptDecryptAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override
    public Object findSerializer(Annotated am) {
        Encrypt annotation = am.getAnnotation(Encrypt.class);
        if (annotation != null) {
            return EncryptDataSerializer.class;
        }
        return null;
    }

    @Override
    public Object findDeserializer(Annotated am) {
        Decrypt annotation = am.getAnnotation(Decrypt.class);
        if (annotation != null) {
            if(am instanceof AnnotatedMethod annotatedMethod){
                return new DecryptDataSerializer(annotatedMethod.getRawParameterType(0),annotation.converter());
            }
        }
        return null;
    }

}