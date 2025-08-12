package cn.hiboot.mcn.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;

/**
 * EmptyStringDeserializer
 *
 * @author DingHao
 * @since 2025/8/12 13:45
 */
public abstract class EmptyStringDeserializer<T> extends JsonDeserializer<T> {

    private final JavaType javaType;

    protected EmptyStringDeserializer(Class<T> clazz) {
        this(TypeFactory.defaultInstance().constructType(clazz));
    }

    protected EmptyStringDeserializer(JavaType javaType) {
        this.javaType = javaType;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null || text.trim().isEmpty()) {
            return getNullValue(ctxt);
        }
        return p.getCodec().readValue(p, javaType);
    }

}