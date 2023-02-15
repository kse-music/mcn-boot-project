package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

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

    public static TypeFactory getTypeFactory(){
        return getObjectMapper().getTypeFactory();
    }

    public static <T> T fromJson(String content, Class<T> clazz){
        try {
            return getObjectMapper().readValue(content,clazz);
        } catch (JsonProcessingException e) {
            throw newInstance(e);
        }
    }

    public static <T> T fromJson(Object content, Class<T> clazz){
        return fromJson(toJson(content),clazz);
    }

    public static <T> T fromJson(String content, TypeReference<T> reference) {
        try {
            return getObjectMapper().readValue(content, reference);
        } catch (Exception e) {
            throw newInstance(e);
        }
    }

    public static <T> T fromJson(Object content, TypeReference<T> reference){
        return fromJson(toJson(content),reference);
    }

    public static <T> T fromJson(String content, JavaType javaType) {
        try {
            return getObjectMapper().readValue(content, javaType);
        } catch (Exception e) {
            throw newInstance(e);
        }
    }

    public static <T> T fromJson(Object content, JavaType javaType){
        return fromJson(toJson(content),javaType);
    }

    public static <T> List<T> fromList(String content,Class<T> clazz){
        return fromJson(content,getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static List<Map<String,Object>> fromListMap(String content){
        return fromJson(content,getTypeFactory().constructCollectionType(List.class,Map.class));
    }

    public static <K, V> List<Map<K,V>> fromListMap(String content,Class<K> keyClass,Class<V> valueClass){
        return fromJson(content,getTypeFactory().constructCollectionType(List.class,getTypeFactory().constructMapType(Map.class,keyClass,valueClass)));
    }

    public static Map<String, Object> fromMap(String content){
        return fromJson(content,new TypeReference<Map<String, Object>>(){});
    }

    public static <K,V> Map<K, V> fromMap(String content,Class<K> keyClass,Class<V> valueClass){
        return fromJson(content,getTypeFactory().constructMapType(Map.class,keyClass,valueClass));
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
