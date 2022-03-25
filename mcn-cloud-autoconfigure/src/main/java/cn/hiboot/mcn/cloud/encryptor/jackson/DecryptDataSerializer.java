package cn.hiboot.mcn.cloud.encryptor.jackson;

import cn.hiboot.mcn.autoconfigure.util.SpringBeanUtils;
import cn.hiboot.mcn.cloud.encryptor.DecryptDataConverter;
import cn.hiboot.mcn.core.exception.ServiceException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.ObjectUtils;

import java.io.IOException;

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

    public DecryptDataSerializer(Class<?> type,Class<? extends DecryptDataConverter> converter) {
        super(type);
        this.textEncryptor = SpringBeanUtils.getBean(TextEncryptor.class);
        this.conversionService = SpringBeanUtils.getBean(ConversionService.class);
        this.converter = converter;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if(converter != DecryptDataConverter.class){
            try {
                DecryptDataConverter decryptDataConverter = converter.newInstance();
                return decryptDataConverter.apply(p,textEncryptor);
            } catch (Exception e){
                throw ServiceException.newInstance("DecryptData Converter Failed",e);
            }
        }
        String currentValue = p.getText();
        if(ObjectUtils.isEmpty(currentValue)){
            return currentValue;
        }
        try{
            currentValue = textEncryptor.decrypt(currentValue);
        }catch (Exception e){
            //
        }
        return conversionService.convert(currentValue,handledType());
    }
}
