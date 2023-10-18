package cn.hiboot.mcn.cloud.client;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * RestClient
 *
 * @author DingHao
 * @since 2023/6/10 12:00
 */
public class RestClient {

    private final Logger log = LoggerFactory.getLogger(RestClient.class);
    private Class<?> wrapperClass = RestResp.class;
    private final RestTemplate restTemplate;

    private RestClient() {
        this(new RestTemplate());
    }

    protected RestClient(Class<?> wrapperClass) {
        this();
        this.wrapperClass = wrapperClass;
    }

    public RestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected RestClient(Class<?> wrapperClass, RestTemplate restTemplate) {
        this.wrapperClass = wrapperClass;
        this.restTemplate = restTemplate;
    }

    public <D> D getObject(String url, Class<D> resultClass) {
        return getObject(url, resultClass, null);
    }

    public <D> D getObject(String url, Class<D> resultClass,Map<String,Object> uriVariables) {
        return get(url, ResolvableType.forClass(resultClass),uriVariables);
    }

    public Map<String,Object> getMap(String url) {
        return getMap(url,null);
    }

    public Map<String,Object> getMap(String url, Map<String,Object> uriVariables) {
        return getMap(url, String.class, Object.class,uriVariables);
    }

    public <K,V> Map<K,V> getMap(String url,Class<K> key, Class<V> value,Map<String,Object> uriVariables) {
        return get(url, ResolvableType.forClassWithGenerics(Map.class,key, value),uriVariables);
    }

    public <D> List<D> getList(String url, Class<D> resultClass) {
        return getList(url, resultClass, null);
    }

    public <D> List<D> getList(String url, Class<D> resultClass, Map<String,Object> uriVariables) {
        return get(url, ResolvableType.forClassWithGenerics(List.class,resultClass),uriVariables);
    }

    public List<Map<String,Object>> getListMap(String url) {
        return getListMap(url, null);
    }

    public List<Map<String,Object>> getListMap(String url,Map<String,Object> uriVariables) {
        return getListMap(url, String.class, Object.class,uriVariables);
    }

    public <K,V> List<Map<String,Object>> getListMap(String url,Class<K> key, Class<V> value,Map<String,Object> uriVariables) {
        return get(url, ResolvableType.forClassWithGenerics(List.class,ResolvableType.forClassWithGenerics(Map.class,key, value)),uriVariables);
    }

    public <D> D get(String url, ResolvableType resultType, Map<String,Object> uriVariables) {
        return exchange(url, HttpMethod.GET, MediaType.APPLICATION_JSON, resultType, null,uriVariables);
    }

    public <A, D> D postObject(String url, Class<D> resultClass, A requestBody) {
        return postObject(url, resultClass, requestBody,null);
    }

    public <A, D> D postObject(String url, Class<D> resultClass, A requestBody, Map<String,Object> uriVariables) {
        return post(url,ResolvableType.forClass(resultClass), requestBody,uriVariables);
    }

    public <A> Map<String,Object> postMap(String url, A requestBody) {
        return postMap(url,requestBody,null);
    }

    public <A> Map<String,Object> postMap(String url, A requestBody, Map<String,Object> uriVariables) {
        return postMap(url, String.class, Object.class,requestBody,uriVariables);
    }

    public <A,K,V> Map<K,V> postMap(String url,Class<K> key, Class<V> value,A requestBody,Map<String,Object> uriVariables) {
        return post(url, ResolvableType.forClassWithGenerics(Map.class,key, value),requestBody,uriVariables);
    }

    public <A, D> List<D> postList(String url, Class<D> resultClass, A requestBody) {
        return postList(url, resultClass, requestBody,null);
    }

    public <A, D> List<D> postList(String url, Class<D> resultClass, A requestBody, Map<String,Object> uriVariables) {
        return post(url,ResolvableType.forClassWithGenerics(List.class,resultClass), requestBody,uriVariables);
    }

    public <A> List<Map<String,Object>> postListMap(String url, A requestBody) {
        return postListMap(url,requestBody,null);
    }

    public <A> List<Map<String,Object>> postListMap(String url, A requestBody, Map<String,Object> uriVariables) {
        return postListMap(url, String.class, Object.class,requestBody,uriVariables);
    }

    public <A,K,V> List<Map<K,V>> postListMap(String url,Class<K> key, Class<V> value,A requestBody,Map<String,Object> uriVariables) {
        return post(url, ResolvableType.forClassWithGenerics(List.class,ResolvableType.forClassWithGenerics(Map.class,key, value)),requestBody,uriVariables);
    }

    public <A, D> D post(String url, ResolvableType resultType, A requestBody, Map<String,Object> uriVariables) {
        return exchange(url, HttpMethod.POST, MediaType.APPLICATION_JSON, resultType, requestBody,uriVariables);
    }

    public <A, D> D exchange(String url, HttpMethod method, MediaType mediaType, ResolvableType resultType, A requestBody, Map<String,Object> uriVariables) {
        return doExchange(url, method, mediaType, resultType, requestBody, uriVariables);
    }

    private <A, D, W> D doExchange(String url, HttpMethod method, MediaType mediaType, ResolvableType resultType, A requestBody, Map<String, ?> uriVariables) {
        if(uriVariables == null){
            uriVariables = Collections.emptyMap();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        HttpEntity<A> entity = new HttpEntity<>(requestBody, headers);
        long startTime = System.currentTimeMillis();
        ResponseEntity<W> resultEntity;
        try {
            resultEntity = restTemplate.exchange(url, method, entity, ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(wrapperClass, resultType).getType()),uriVariables);
        } catch (RestClientException e) {
            logError(url, requestBody, e.getMessage());
            throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR);
        }
        log.debug("url={}, cost time={}ms, inputParam={}", url, System.currentTimeMillis() - startTime, JacksonUtils.toJson(requestBody));
        if (resultEntity.getStatusCode() != HttpStatus.OK) {
            throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR);
        }
        try{
            if(resultEntity.hasBody()){
                D response = response(resultEntity.getBody());
                log.debug("response={}",JacksonUtils.toJson(response));
                return response;
            }
            return null;
        }catch (ServiceException e){
            logError(url, requestBody, e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    protected <D,W> D response(W body){
        RestResp restResp = (RestResp)body;
        if (restResp.isFailed()) {
            throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR,restResp.getErrorInfo());
        }
        return (D) restResp.getData();
    }

    private void logError(String url, Object requestBody, String errorMsg){
        log.error("url={}, inputParam={}, errorInfo={}", url,JacksonUtils.toJson(requestBody), errorMsg);
    }

}
