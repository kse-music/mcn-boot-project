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
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * RestClient
 *
 * @author DingHao
 * @since 2023/6/10 12:00
 */
public class RestClient {

    protected final Logger log = LoggerFactory.getLogger(RestClient.class);
    private Class<?> wrapperClass = RestResp.class;
    private final RestTemplate restTemplate;

    public RestClient() {
        this(new RestTemplate());
    }

    public RestClient(RestTemplate restTemplate) {
        restTemplate.getInterceptors().add(new HttpRequestInterceptor(log));
        this.restTemplate = restTemplate;
    }

    protected RestClient(Class<?> wrapperClass) {
        this();
        this.wrapperClass = wrapperClass;
    }

    protected RestClient(Class<?> wrapperClass, RestTemplate restTemplate) {
        this(restTemplate);
        this.wrapperClass = wrapperClass;
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
        return get(url, forClass(resultClass), consumer, uriVariables);
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
        return get(url, forClassWithGenerics(List.class, forClassWithGenerics(Map.class, key, value)), consumer, uriVariables);
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

    private ResolvableType forClass(Class<?> clazz) {
        return ResolvableType.forClass(clazz);
    }

    private ResolvableType forClassWithGenerics(Class<?> clazz, Class<?>... generics) {
        return ResolvableType.forClassWithGenerics(clazz, generics);
    }

    private ResolvableType forClassWithGenerics(Class<?> clazz, ResolvableType... generics) {
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
        return post(url, forClass(resultClass), requestBody, consumer, uriVariables);
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
        return post(url, forClassWithGenerics(List.class, forClassWithGenerics(Map.class, key, value)), requestBody, consumer, uriVariables);
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

    private <A, D, W> D doExchange(String url, HttpMethod method, Consumer<HttpHeaders> headersConsumer, ResolvableType resultType, A requestBody, Map<String, ?> uriVariables) {
        if (uriVariables == null) {
            uriVariables = Collections.emptyMap();
        }
        if (headersConsumer == null) {
            headersConsumer = headers -> headers.setContentType(MediaType.APPLICATION_JSON);
        }
        HttpHeaders headers = new HttpHeaders();
        headersConsumer.accept(headers);
        return processResponse(restTemplate.exchange(url, method, new HttpEntity<>(requestBody, headers), ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(wrapperClass, resultType).getType()), uriVariables), url, requestBody);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <D, W> D extractData(W body) {
        RestResp restResp = (RestResp) body;
        if (restResp.isFailed()) {
            throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR, restResp.getErrorInfo());
        }
        return (D) restResp.getData();
    }

    static class HttpRequestInterceptor implements ClientHttpRequestInterceptor {
        private final Logger log;

        public HttpRequestInterceptor(Logger log) {
            this.log = log;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            long startTime = System.currentTimeMillis();
            ClientHttpResponse response;
            try {
                response = execution.execute(request, body);
                if (log.isDebugEnabled()) {
                    log.debug("url={}, cost time={}ms, inputParam={}", request.getURI(), System.currentTimeMillis() - startTime, new String(body));
                }
            } catch (IOException ex) {
                log.error("url={}, cost time={}ms, inputParam={}, errorInfo={}", request.getURI(), System.currentTimeMillis() - startTime, new String(body), ex.getMessage());
                throw ex;
            }
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("url={}, cost time={}ms, inputParam={}, statusCode={}", request.getURI(), System.currentTimeMillis() - startTime, new String(body), response.getStatusCode());
                throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR);
            }
            return response;
        }
    }

}
