package cn.hiboot.mcn.core.jackson;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * EmptyStringMapDeserializer
 *
 * @author DingHao
 * @since 2025/8/12 14:06
 */
public final class EmptyStringMapDeserializer<K,V> extends EmptyStringDeserializer<Map<K, V>> implements ContextualDeserializer {

    private final boolean emptyMap;

    public EmptyStringMapDeserializer() {
        this(String.class, Object.class, true);
    }

    private EmptyStringMapDeserializer(Class<?> keyClass, Class<?> valueClass, boolean emptyMap) {
        super(TypeFactory.defaultInstance().constructMapType(Map.class, keyClass, valueClass));
        this.emptyMap = emptyMap;
    }

    @Override
    public Map<K, V> getNullValue(DeserializationContext ctxt) {
        return this.emptyMap ? Collections.emptyMap() : new HashMap<>();
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            MapTypeDeserialize ann = beanProperty.getAnnotation(MapTypeDeserialize.class);
            if (ann == null) {
                ann = beanProperty.getContextAnnotation(MapTypeDeserialize.class);
            }
            if (ann != null) {
                return new EmptyStringMapDeserializer<>(ann.keyClass(), ann.valueClass(), ann.emptyMap());
            }
        }
        return this;
    }
}
