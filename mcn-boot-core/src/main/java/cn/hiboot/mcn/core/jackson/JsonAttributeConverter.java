package cn.hiboot.mcn.core.jackson;

import cn.hiboot.mcn.core.util.JacksonUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JsonAttributeConverter
 *
 * @author DingHao
 * @since 2025/2/24 14:19
 */
@Converter
public abstract class JsonAttributeConverter<T> implements AttributeConverter<T, String> {

    private final JavaType javaType;

    public JsonAttributeConverter(JavaType javaType) {
        this.javaType = javaType;
    }

    @Override
    public String convertToDatabaseColumn(T t) {
        return JacksonUtils.toJson(t);
    }

    @Override
    public T convertToEntityAttribute(String str) {
        return JacksonUtils.toBean(str, javaType);
    }

    protected static TypeFactory getTypeFactory() {
        return JacksonUtils.getTypeFactory();
    }

}