package cn.hiboot.mcn.cloud.encryptor.jackson;

import cn.hiboot.mcn.cloud.encryptor.DecryptDataConverter;
import cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * DecryptDataSerializer
 *
 * @author DingHao
 * @since 2022/2/17 14:49
 */
public class DecryptDataSerializer extends StdDeserializer<Object> {

    private final Class<? extends DecryptDataConverter> converter;
    private final TextEncryptor textEncryptor;
    private final ConversionService conversionService;

    public DecryptDataSerializer(Class<?> type, Class<? extends DecryptDataConverter> converter) {
        super(type);
        this.textEncryptor = SpringBeanUtils.getBean(TextEncryptor.class);
        this.conversionService = SpringBeanUtils.getBean(ConversionService.class);
        this.converter = converter;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        if (converter != DecryptDataConverter.class) {
            try {
                DecryptDataConverter decryptDataConverter = ReflectionUtils.accessibleConstructor(converter).newInstance();
                return decryptDataConverter.apply(p, textEncryptor);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw ServiceException.newInstance("DecryptData Converter newInstance Failed", e);
            } catch (IOException e) {
                throw ServiceException.newInstance("Jackson deserialize Failed", e);
            } catch (Exception e) {
                throw ServiceException.newInstance("DecryptData Converter Failed", e);
            }
        }
        String currentValue = p.getString();
        if (ObjectUtils.isEmpty(currentValue)) {
            return currentValue;
        }
        try {
            currentValue = textEncryptor.decrypt(currentValue);
        } catch (Exception e) {
            //
        }
        return conversionService.convert(currentValue, handledType());
    }
}
