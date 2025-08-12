package cn.hiboot.mcn.core.jackson;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EmptyStringListDeserializer
 *
 * @author DingHao
 * @since 2025/8/12 13:45
 */
public final class EmptyStringListDeserializer<T> extends EmptyStringDeserializer<List<T>> implements ContextualDeserializer {

    private final boolean emptyList;

    public EmptyStringListDeserializer() {
        this(Object.class, true);
    }

    private EmptyStringListDeserializer(Class<?> clazz, boolean emptyList) {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
        this.emptyList = emptyList;
    }

    @Override
    public List<T> getNullValue(DeserializationContext ctxt) {
        return this.emptyList ? Collections.emptyList() : new ArrayList<>();
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            ListTypeDeserialize ann = beanProperty.getAnnotation(ListTypeDeserialize.class);
            if (ann == null) {
                ann = beanProperty.getContextAnnotation(ListTypeDeserialize.class);
            }
            if (ann != null) {
                return new EmptyStringListDeserializer<>(ann.value(), ann.emptyList());
            }
        }
        return this;
    }

}