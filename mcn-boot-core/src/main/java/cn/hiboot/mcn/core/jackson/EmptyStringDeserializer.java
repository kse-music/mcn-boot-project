package cn.hiboot.mcn.core.jackson;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.type.TypeFactory;

/**
 * EmptyStringDeserializer
 *
 * @author DingHao
 * @since 2025/8/12 13:45
 */
public abstract class EmptyStringDeserializer<T> extends ValueDeserializer<T> {

    private final JavaType javaType;

    protected EmptyStringDeserializer(Class<T> clazz) {
        this(TypeFactory.createDefaultInstance().constructType(clazz));
    }

    protected EmptyStringDeserializer(JavaType javaType) {
        this.javaType = javaType;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String text = p.getString();
        if (text == null || text.trim().isEmpty()) {
            @SuppressWarnings("unchecked")
            T nullValue = (T) getNullValue(ctxt);
            return nullValue;
        }
        return ctxt.readValue(p, javaType);
    }

}
