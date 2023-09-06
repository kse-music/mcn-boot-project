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
 * Remoter
 *
 * @author DingHao
 * @since 2023/6/10 12:00
 */
public class RestClient {

    private final Logger log = LoggerFactory.getLogger(RestClient.class);

    private final RestTemplate restTemplate;

    public RestClient() {
        this(new RestTemplate());
    }

    public RestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <R> R get(String url, Class<?> resultClass) {
        return get(url, resultClass, Collections.emptyMap());
    }

    public <R> R get(String url, Class<?> resultClass,Map<String,Object> uriVariables) {
        return exchange(url, HttpMethod.GET, MediaType.APPLICATION_JSON, buildResultType(resultClass,false), null,uriVariables);
    }

    public <R> List<R> getList(String url, Class<?> resultClass) {
        return getList(url, resultClass, Collections.emptyMap());
    }

    public <A, R> List<R> getList(String url, Class<?> resultClass, Map<String,Object> uriVariables) {
        return exchange(url, HttpMethod.GET, MediaType.APPLICATION_JSON, buildResultType(resultClass,true), null,uriVariables);
    }

    public <A, R> R post(String url, Class<?> resultClass, A requestBody) {
        return post(url, resultClass, requestBody,Collections.emptyMap());
    }

    public <A, R> R post(String url, Class<?> resultClass, A requestBody, Map<String,Object> uriVariables) {
        return exchange(url, HttpMethod.POST, MediaType.APPLICATION_JSON, buildResultType(resultClass,false), requestBody,uriVariables);
    }

    public <A, R> List<R> postList(String url, Class<?> resultClass, A requestBody) {
        return postList(url, resultClass, requestBody,Collections.emptyMap());
    }

    public <A, R> List<R> postList(String url, Class<?> resultClass, A requestBody, Map<String,Object> uriVariables) {
        return exchange(url, HttpMethod.POST, MediaType.APPLICATION_JSON, buildResultType(resultClass,true), requestBody,uriVariables);
    }

    public <A, R> R exchange(String url, HttpMethod method, MediaType mediaType, Class<?> resultClass, A requestBody) {
        return exchange(url, method, mediaType, buildResultType(resultClass,false), requestBody, Collections.emptyMap());
    }

    public <A, R> List<R> exchangeList(String url, HttpMethod method, MediaType mediaType, Class<?> resultClass, A requestBody) {
        return exchange(url, method, mediaType, buildResultType(resultClass,true), requestBody, Collections.emptyMap());
    }

    public <A> Object exchangeObject(String url, HttpMethod method, MediaType mediaType, Class<?> resultClass, A requestBody) {
        if (List.class.isAssignableFrom(resultClass)) {
            return exchangeList(url, method, mediaType, Map.class, requestBody);
        }
        return exchange(url,method,mediaType,resultClass,requestBody);
    }

    private <T> ParameterizedTypeReference<T> buildResultType(Class<?> resultClass,boolean isList) {
        if(isList){
            return ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(RestResp.class, ResolvableType.forClassWithGenerics(List.class,resultClass)).getType());
        }
        return ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(RestResp.class,resultClass).getType());
    }

    private <A, R> R exchange(String url, HttpMethod method, MediaType mediaType, ParameterizedTypeReference<RestResp<R>> responseBodyType, A requestBody, Map<String, ?> uriVariables) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        HttpEntity<A> entity = new HttpEntity<>(requestBody, headers);
        long startTime = System.currentTimeMillis();
        ResponseEntity<RestResp<R>> resultEntity;
        try {
            resultEntity = restTemplate.exchange(url, method, entity, responseBodyType,uriVariables);
        } catch (RestClientException e) {
            logError(url, requestBody, e.getMessage());
            throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR);
        }
        RestResp<R> body = resultEntity.getBody();
        log.debug("url={}, cost time={}ms, inputParam={}, response={}", url, System.currentTimeMillis() - startTime, JacksonUtils.toJson(requestBody), JacksonUtils.toJson(body));
        if (resultEntity.getStatusCode() != HttpStatus.OK || body == null || body.isFailed()) {
            String errorInfo = body == null ? resultEntity.getStatusCode().toString() : body.getErrorInfo();
            logError(url, requestBody, errorInfo);
            throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR,errorInfo);
        }
        return body.getData();
    }

    private void logError(String url, Object requestBody, String errorMsg){
        log.error("url={}, inputParam={}, errorInfo={}", url,JacksonUtils.toJson(requestBody), errorMsg);
    }

}
