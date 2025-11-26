package cn.hiboot.mcn.core.jackson;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.type.TypeFactory;

/**
 * EmptyStringDeserializer
 *
 * @author DingHao
 * @since 2025/8/12 13:45
 */
public abstract class EmptyStringDeserializer<T> extends StdDeserializer<T> {

    private static final TypeFactory typeFactory = TypeFactory.createDefaultInstance();

    protected EmptyStringDeserializer(Class<T> clazz) {
        super(clazz);
    }

    protected EmptyStringDeserializer(JavaType javaType) {
        super(javaType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getNullValue(DeserializationContext ctxt) {
        return (T) super.getNullValue(ctxt);
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String text = p.getString();
        if (text == null || text.trim().isEmpty()) {
            return getNullValue(ctxt);
        }
        return p.readValueAs(getValueType());
    }

    protected static TypeFactory defaultInstance() {
        return typeFactory;
    }

}