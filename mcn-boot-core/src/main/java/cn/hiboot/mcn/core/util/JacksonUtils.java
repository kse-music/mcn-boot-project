package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.model.JsonArray;
import cn.hiboot.mcn.core.model.JsonObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * JacksonUtils
 *
 * @author DingHao
 * @since 2021/7/7 21:02
 */
public abstract class JacksonUtils {

    private static ObjectMapper objectMapper;

    public static void setObjectMapper(ObjectMapper objectMapper) {
        JacksonUtils.objectMapper = objectMapper;
    }

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
        return objectMapper;
    }

    public static TypeFactory getTypeFactory() {
        return getObjectMapper().getTypeFactory();
    }

    public static <T> T fromJson(Object input, Class<T> clazz) {
        return fromJson(input, getTypeFactory().constructType(clazz));
    }

    public static <T> T fromJson(Object input, TypeReference<T> typeRef) {
        return fromJson(input, getTypeFactory().constructType(typeRef));
    }

    public static <T> T fromJson(Object input, JavaType javaType) {
        ObjectMapper mapper = getObjectMapper();
        try {
            if (input instanceof String) {
                return mapper.readValue(input.toString(), javaType);
            } else if (input instanceof byte[]) {
                return mapper.readValue((byte[]) input, javaType);
            } else {
                return mapper.convertValue(input, javaType);
            }
        } catch (Exception e) {
            throw newInstance(e);
        }
    }

    public static <T> List<T> fromList(Object input, Class<T> clazz) {
        return fromJson(input, getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static List<Map<String, Object>> fromListMap(Object input) {
        return fromJson(input, getTypeFactory().constructCollectionType(List.class, Map.class));
    }

    public static <K, V> List<Map<K, V>> fromListMap(Object input, Class<K> keyClass, Class<V> valueClass) {
        return fromJson(input, getTypeFactory().constructCollectionType(List.class, getTypeFactory().constructMapType(Map.class, keyClass, valueClass)));
    }

    public static Map<String, Object> fromMap(Object input) {
        return fromJson(input, new TypeReference<Map<String, Object>>() {
        });
    }

    public static <K, V> Map<K, V> fromMap(Object input, Class<K> keyClass, Class<V> valueClass) {
        return fromJson(input, getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
    }

    public static String toJson(Object input) {
        try {
            return getObjectMapper().writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static JsonObject jsonObject(Object input) {
        try {
            return new JsonObject((ObjectNode) getObjectMapper().readTree(valueString(input)));
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static JsonArray jsonArray(Object input) {
        try {
            return new JsonArray((ArrayNode) getObjectMapper().readTree(valueString(input)));
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    private static String valueString(Object input) {
        if (input instanceof byte[]) {
            return new String((byte[]) input, StandardCharsets.UTF_8);
        }
        return input instanceof String ? input.toString() : toJson(input);
    }

    private static ServiceException newInstance(Throwable cause) {
        ServiceException jsonException = ServiceException.newInstance(ErrorMsg.getErrorMsg(ExceptionKeys.JSON_PARSE_ERROR), cause);
        jsonException.setCode(ExceptionKeys.JSON_PARSE_ERROR);
        return jsonException;
    }

}
