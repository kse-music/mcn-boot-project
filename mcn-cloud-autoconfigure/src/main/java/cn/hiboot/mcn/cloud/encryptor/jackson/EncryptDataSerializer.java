package cn.hiboot.mcn.cloud.encryptor.jackson;

import cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.springframework.beans.BeanUtils;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.util.Objects;

/**
 * EncryptDataSerializer
 *
 * @author DingHao
 * @since 2022/2/17 14:49
 */
public class EncryptDataSerializer extends ValueSerializer<Object> {

    private final TextEncryptor textEncryptor;
    private final static ObjectMapper objectMapper = JsonMapper.shared();

    public EncryptDataSerializer() {
        this.textEncryptor = SpringBeanUtils.getBean(TextEncryptor.class);
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext context) throws tools.jackson.core.JacksonException {
        if (Objects.isNull(value)) {
            return;
        }
        String rs = value.toString();
        if (!BeanUtils.isSimpleProperty(value.getClass())) {//如果不是简单类型直接转json
            rs = objectMapper.writeValueAsString(value);
        }
        gen.writeString(textEncryptor.encrypt(rs));
    }

}
