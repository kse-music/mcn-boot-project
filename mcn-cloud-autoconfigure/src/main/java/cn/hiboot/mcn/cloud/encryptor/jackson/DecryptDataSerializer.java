package cn.hiboot.mcn.cloud.encryptor.jackson;

import cn.hiboot.mcn.autoconfigure.util.SpringBeanUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.io.IOException;

/**
 * DecryptDataSerializer
 *
 * @author DingHao
 * @since 2022/2/17 14:49
 */
public class DecryptDataSerializer extends StdDeserializer<Object> {

    private final TextEncryptor textEncryptor;
    private final ConversionService conversionService;
    private final Class<?> type;

    public DecryptDataSerializer(Class<?> type) {
        super(Object.class);
        this.textEncryptor = SpringBeanUtils.getBean(TextEncryptor.class);
        this.conversionService = SpringBeanUtils.getBean(ConversionService.class);
        this.type = type == null ? String.class : type;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String currentValue = p.getText();
        try{
            currentValue = textEncryptor.decrypt(p.getText());
        }catch (Exception e){
            //
        }
        return conversionService.convert(currentValue,type);
    }
}
