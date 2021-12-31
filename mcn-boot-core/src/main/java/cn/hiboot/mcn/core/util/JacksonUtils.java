package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public static void setObjectMapper(ObjectMapper objectMapper){
        JacksonUtils.objectMapper = objectMapper;
    }

    public static ObjectMapper getObjectMapper() {
        if(objectMapper == null){
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
        return objectMapper;
    }

    public static <T> T fromJson(String content, Class<T> clazz){
        try {
            return getObjectMapper().readValue(content,clazz);
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static <T> List<T> fromList(String content,Class<T> clazz){
        try {
            return getObjectMapper().readValue(content,getObjectMapper().getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static <K, V> List<Map<K,V>> fromListMap(String content,Class<K> keyClass,Class<V> valueClass){
        try {
            return getObjectMapper().readValue(content,getObjectMapper().getTypeFactory().constructCollectionType(List.class,
                    getObjectMapper().getTypeFactory().constructMapType(Map.class,keyClass,valueClass)));
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static Map<String, Object> fromMap(String content){
        try {
            return getObjectMapper().readValue(content,new TypeReference<Map<String, Object>>(){});
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static <K,V> Map<K, V> fromMap(String content,Class<K> keyClass,Class<V> valueClass){
        try {
            return getObjectMapper().readValue(content,getObjectMapper().getTypeFactory().constructMapType(Map.class,keyClass,valueClass));
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> reference) {
        try {
            return JacksonUtils.getObjectMapper().readValue(json, reference);
        } catch (Exception e) {
            throw newInstance(e);
        }
    }

    public static String toJson(Object value){
        try {
            return getObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    private static ServiceException newInstance(Throwable cause){
        ServiceException jsonException = ServiceException.newInstance(ErrorMsg.getErrorMsg(ExceptionKeys.JSON_PARSE_ERROR),cause);
        jsonException.setCode(ExceptionKeys.JSON_PARSE_ERROR);
        return jsonException;
    }

}
