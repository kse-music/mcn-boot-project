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

    private static String valueString(Object content) {
        if (content instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return content instanceof String value ? value : toJson(content);
    }

    public static <T> T fromJson(Object content, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(valueString(content), clazz);
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static <T> T fromJson(Object content, TypeReference<T> reference) {
        try {
            return getObjectMapper().readValue(valueString(content), reference);
        } catch (Exception e) {
            throw newInstance(e);
        }
    }

    public static <T> T fromJson(Object content, JavaType javaType) {
        try {
            return getObjectMapper().readValue(valueString(content), javaType);
        } catch (Exception e) {
            throw newInstance(e);
        }
    }

    public static <T> List<T> fromList(Object content, Class<T> clazz) {
        return fromJson(valueString(content), getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static List<Map<String, Object>> fromListMap(Object content) {
        return fromJson(valueString(content), getTypeFactory().constructCollectionType(List.class, Map.class));
    }

    public static <K, V> List<Map<K, V>> fromListMap(Object content, Class<K> keyClass, Class<V> valueClass) {
        return fromJson(valueString(content), getTypeFactory().constructCollectionType(List.class, getTypeFactory().constructMapType(Map.class, keyClass, valueClass)));
    }

    public static Map<String, Object> fromMap(Object content) {
        return fromJson(valueString(content), new TypeReference<Map<String, Object>>() {
        });
    }

    public static <K, V> Map<K, V> fromMap(Object content, Class<K> keyClass, Class<V> valueClass) {
        return fromJson(valueString(content), getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
    }

    public static String toJson(Object value) {
        try {
            return getObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static JsonObject jsonObject(Object content) {
        try {
            return new JsonObject((ObjectNode) getObjectMapper().readTree(valueString(content)));
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static JsonArray jsonArray(Object content) {
        try {
            return new JsonArray((ArrayNode) getObjectMapper().readTree(valueString(content)));
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    private static ServiceException newInstance(Throwable cause) {
        ServiceException jsonException = ServiceException.newInstance(ErrorMsg.getErrorMsg(ExceptionKeys.JSON_PARSE_ERROR), cause);
        jsonException.setCode(ExceptionKeys.JSON_PARSE_ERROR);
        return jsonException;
    }

}
