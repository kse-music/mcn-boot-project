package cn.hiboot.mcn.cloud.client;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * RestClient
 *
 * @author DingHao
 * @since 2023/6/10 12:00
 */
public class RestClient {

    protected final Logger log = LoggerFactory.getLogger(RestClient.class);
    private final Class<?> wrapperClass;
    private final Function<Object, Object> extractData;
    private final RestTemplate restTemplate;
    private Consumer<HttpHeaders> defaultHeaders = headers -> headers.setContentType(MediaType.APPLICATION_JSON);

    public RestClient() {
        this(new RestTemplate());
    }

    public RestClient(RestTemplate restTemplate) {
        this(RestResp.class, null, restTemplate);
    }

    public RestClient(Class<?> wrapperClass, Function<Object, Object> extractData) {
        this(wrapperClass, extractData, new RestTemplate());
    }

    public RestClient(Class<?> wrapperClass, Function<Object, Object> extractData, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.wrapperClass = wrapperClass;
        this.extractData = extractData;
    }

    public void setDefaultHeaders(Consumer<HttpHeaders> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public <D> D getObject(String url, Class<D> resultClass) {
        return getObject(url, resultClass, null, null);
    }

    public <D> D getObject(String url, Class<D> resultClass, Consumer<HttpHeaders> consumer) {
        return getObject(url, resultClass, consumer, null);
    }

    public <D> D getObject(String url, Class<D> resultClass, Map<String, Object> uriVariables) {
        return getObject(url, resultClass, null, uriVariables);
    }

    public <D> D getObject(String url, Class<D> resultClass, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return get(url, ResolvableType.forClass(resultClass), consumer, uriVariables);
    }

    public Map<String, Object> getMap(String url) {
        return getMap(url, null, null);
    }

    public Map<String, Object> getMap(String url, Consumer<HttpHeaders> consumer) {
        return getMap(url, consumer, null);
    }

    public Map<String, Object> getMap(String url, Map<String, Object> uriVariables) {
        return getMap(url, null, uriVariables);
    }

    public Map<String, Object> getMap(String url, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return getMap(url, String.class, Object.class, consumer, uriVariables);
    }

    public <K, V> Map<K, V> getMap(String url, Class<K> key, Class<V> value, Map<String, Object> uriVariables) {
        return getMap(url, key, value, null, uriVariables);
    }

    public <K, V> Map<K, V> getMap(String url, Class<K> key, Class<V> value, Consumer<HttpHeaders> consumer) {
        return getMap(url, key, value, consumer, null);
    }

    public <K, V> Map<K, V> getMap(String url, Class<K> key, Class<V> value, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return get(url, forClassWithGenerics(Map.class, key, value), consumer, uriVariables);
    }

    public <D> List<D> getList(String url, Class<D> resultClass) {
        return getList(url, resultClass, null, null);
    }

    public <D> List<D> getList(String url, Class<D> resultClass, Consumer<HttpHeaders> consumer) {
        return getList(url, resultClass, consumer, null);
    }

    public <D> List<D> getList(String url, Class<D> resultClass, Map<String, Object> uriVariables) {
        return getList(url, resultClass, null, uriVariables);
    }

    public <D> List<D> getList(String url, Class<D> resultClass, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return get(url, forClassWithGenerics(List.class, resultClass), consumer, uriVariables);
    }

    public List<Map<String, Object>> getListMap(String url) {
        return getListMap(url, null, null);
    }

    public List<Map<String, Object>> getListMap(String url, Map<String, Object> uriVariables) {
        return getListMap(url, null, uriVariables);
    }

    public List<Map<String, Object>> getListMap(String url, Consumer<HttpHeaders> consumer) {
        return getListMap(url, consumer, null);
    }

    public List<Map<String, Object>> getListMap(String url, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return getListMap(url, String.class, Object.class, consumer, uriVariables);
    }

    public <K, V> List<Map<K, V>> getListMap(String url, Class<K> key, Class<V> value, Consumer<HttpHeaders> consumer) {
        return getListMap(url, key, value, consumer, null);
    }

    public <K, V> List<Map<K, V>> getListMap(String url, Class<K> key, Class<V> value, Map<String, Object> uriVariables) {
        return getListMap(url, key, value, null, uriVariables);
    }

    public <K, V> List<Map<K, V>> getListMap(String url, Class<K> key, Class<V> value, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return get(url, ResolvableType.forClassWithGenerics(List.class, forClassWithGenerics(Map.class, key, value)), consumer, uriVariables);
    }

    public <D> D get(String url, ResolvableType resultType, Map<String, Object> uriVariables) {
        return get(url, resultType, null, uriVariables);
    }

    public <D> D get(String url, ResolvableType resultType, Consumer<HttpHeaders> consumer) {
        return get(url, resultType, consumer, null);
    }

    public <D> D get(String url, ResolvableType resultType, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return exchange(url, HttpMethod.GET, consumer, resultType, null, uriVariables);
    }

    private ResolvableType forClassWithGenerics(Class<?> clazz, Class<?>... generics) {
        return ResolvableType.forClassWithGenerics(clazz, generics);
    }

    public <A, D> D postObject(String url, Class<D> resultClass, A requestBody) {
        return postObject(url, resultClass, requestBody, null, null);
    }

    public <A, D> D postObject(String url, Class<D> resultClass, A requestBody, Consumer<HttpHeaders> consumer) {
        return postObject(url, resultClass, requestBody, consumer, null);
    }

    public <A, D> D postObject(String url, Class<D> resultClass, A requestBody, Map<String, Object> uriVariables) {
        return postObject(url, resultClass, requestBody, null, uriVariables);
    }

    public <A, D> D postObject(String url, Class<D> resultClass, A requestBody, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return post(url, ResolvableType.forClass(resultClass), requestBody, consumer, uriVariables);
    }

    public <A> Map<String, Object> postMap(String url, A requestBody) {
        return postMap(url, requestBody, null, null);
    }

    public <A> Map<String, Object> postMap(String url, A requestBody, Consumer<HttpHeaders> consumer) {
        return postMap(url, requestBody, consumer, null);
    }

    public <A> Map<String, Object> postMap(String url, A requestBody, Map<String, Object> uriVariables) {
        return postMap(url, requestBody, null, uriVariables);
    }

    public <A> Map<String, Object> postMap(String url, A requestBody, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return postMap(url, String.class, Object.class, requestBody, consumer, uriVariables);
    }

    public <A, K, V> Map<K, V> postMap(String url, Class<K> key, Class<V> value, A requestBody, Consumer<HttpHeaders> consumer) {
        return postMap(url, key, value, requestBody, consumer, null);
    }

    public <A, K, V> Map<K, V> postMap(String url, Class<K> key, Class<V> value, A requestBody, Map<String, Object> uriVariables) {
        return postMap(url, key, value, requestBody, null, uriVariables);
    }

    public <A, K, V> Map<K, V> postMap(String url, Class<K> key, Class<V> value, A requestBody, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return post(url, forClassWithGenerics(Map.class, key, value), requestBody, consumer, uriVariables);
    }

    public <A, D> List<D> postList(String url, Class<D> resultClass, A requestBody) {
        return postList(url, resultClass, requestBody, null, null);
    }

    public <A, D> List<D> postList(String url, Class<D> resultClass, A requestBody, Consumer<HttpHeaders> consumer) {
        return postList(url, resultClass, requestBody, consumer, null);
    }

    public <A, D> List<D> postList(String url, Class<D> resultClass, A requestBody, Map<String, Object> uriVariables) {
        return postList(url, resultClass, requestBody, null, uriVariables);
    }

    public <A, D> List<D> postList(String url, Class<D> resultClass, A requestBody, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return post(url, forClassWithGenerics(List.class, resultClass), requestBody, consumer, uriVariables);
    }

    public <A> List<Map<String, Object>> postListMap(String url, A requestBody) {
        return postListMap(url, requestBody, null, null);
    }

    public <A> List<Map<String, Object>> postListMap(String url, A requestBody, Consumer<HttpHeaders> consumer) {
        return postListMap(url, requestBody, consumer, null);
    }

    public <A> List<Map<String, Object>> postListMap(String url, A requestBody, Map<String, Object> uriVariables) {
        return postListMap(url, requestBody, null, uriVariables);
    }

    public <A> List<Map<String, Object>> postListMap(String url, A requestBody, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return postListMap(url, String.class, Object.class, requestBody, consumer, uriVariables);
    }

    public <A, K, V> List<Map<K, V>> postListMap(String url, Class<K> key, Class<V> value, A requestBody, Consumer<HttpHeaders> consumer) {
        return postListMap(url, key, value, requestBody, consumer, null);
    }

    public <A, K, V> List<Map<K, V>> postListMap(String url, Class<K> key, Class<V> value, A requestBody, Map<String, Object> uriVariables) {
        return postListMap(url, key, value, requestBody, null, uriVariables);
    }

    public <A, K, V> List<Map<K, V>> postListMap(String url, Class<K> key, Class<V> value, A requestBody, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return post(url, ResolvableType.forClassWithGenerics(List.class, forClassWithGenerics(Map.class, key, value)), requestBody, consumer, uriVariables);
    }

    public <A, D> D post(String url, ResolvableType resultType, A requestBody, Consumer<HttpHeaders> consumer) {
        return post(url, resultType, requestBody, consumer, null);
    }

    public <A, D> D post(String url, ResolvableType resultType, A requestBody, Map<String, Object> uriVariables) {
        return post(url, resultType, requestBody, null, uriVariables);
    }

    public <A, D> D post(String url, ResolvableType resultType, A requestBody, Consumer<HttpHeaders> consumer, Map<String, Object> uriVariables) {
        return exchange(url, HttpMethod.POST, consumer, resultType, requestBody, uriVariables);
    }

    public <A, D> D exchange(String url, HttpMethod method, Consumer<HttpHeaders> headersConsumer, ResolvableType resultType, A requestBody, Map<String, Object> uriVariables) {
        return doExchange(url, method, headersConsumer, resultType, requestBody, uriVariables);
    }

    private ResolvableType resultClass(ResolvableType resultType) {
        return this.wrapperClass == null ? resultType : ResolvableType.forClassWithGenerics(this.wrapperClass, resultType);
    }

    private <A, D, W> D doExchange(String url, HttpMethod method, Consumer<HttpHeaders> headersConsumer, ResolvableType resultType, A requestBody, Map<String, ?> uriVariables) {
        if (uriVariables == null) {
            uriVariables = Collections.emptyMap();
        }
        HttpHeaders headers = new HttpHeaders();
        this.defaultHeaders.accept(headers);
        if (headersConsumer != null) {
            headersConsumer.accept(headers);
        }
        long startTime = System.currentTimeMillis();
        ResponseEntity<W> response;
        try {
            response = restTemplate.exchange(url, method, new HttpEntity<>(requestBody, headers),
                    ParameterizedTypeReference.forType(resultClass(resultType).getType()), uriVariables);
            if (log.isDebugEnabled()) {
                log.debug("url={}, cost time={}ms, inputParam={}", url, System.currentTimeMillis() - startTime, requestBody);
            }
        } catch (Exception ex) {
            log.error("url={}, cost time={}ms, inputParam={}, errorInfo={}", url, System.currentTimeMillis() - startTime, requestBody, ex.getMessage());
            throw ex;
        }
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("url={}, cost time={}ms, inputParam={}, statusCode={}", url, System.currentTimeMillis() - startTime, requestBody, response.getStatusCode());
            throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR);
        }
        return processResponse(response, url, requestBody);
    }

    protected <A, D, W> D processResponse(ResponseEntity<W> responseEntity, String url, A requestBody) {
        try {
            if (responseEntity.hasBody()) {
                D response = extractData(responseEntity.getBody());
                if (log.isDebugEnabled()) {
                    log.debug("response={}", JacksonUtils.toJson(response));
                }
                return response;
            }
            return null;
        } catch (ServiceException ex) {
            log.error("url={}, inputParam={}, errorInfo={}", url, JacksonUtils.toJson(requestBody), ex.getMessage());
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    protected <D, W> D extractData(W body) {
        if (this.extractData != null) {
            return (D) this.extractData.apply(body);
        }
        RestResp<D> restResp = (RestResp<D>) body;
        if (restResp.isFailed()) {
            throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR, restResp.getErrorInfo());
        }
        return restResp.getData();
    }

}
