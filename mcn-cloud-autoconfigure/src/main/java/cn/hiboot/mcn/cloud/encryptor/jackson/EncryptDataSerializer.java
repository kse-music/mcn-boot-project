package cn.hiboot.mcn.cloud.encryptor.jackson;

import cn.hiboot.mcn.core.util.SpringBeanUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.io.IOException;
import java.util.Objects;

/**
 * EncryptDataSerializer
 *
 * @author DingHao
 * @since 2022/2/17 14:49
 */
public class EncryptDataSerializer extends StdSerializer<Object> {

    private final TextEncryptor textEncryptor;
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public EncryptDataSerializer() {
        super(Object.class);
        this.textEncryptor = SpringBeanUtils.getBean(TextEncryptor.class);
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (Objects.isNull(value)) {
            return;
        }
        String rs = value.toString();
        if(!BeanUtils.isSimpleProperty(value.getClass())){//如果不是简单类型直接转json
            rs = objectMapper.writeValueAsString(value);
        }
        gen.writeString(textEncryptor.encrypt(rs));
    }

}
