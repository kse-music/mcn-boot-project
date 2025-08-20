package cn.hiboot.mcn.core.client;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.model.JsonArray;
import cn.hiboot.mcn.core.model.JsonObject;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
public final class RestClient {

    private final Logger log = LoggerFactory.getLogger(RestClient.class);
    private final Builder builder;

    public RestClient(Builder builder) {
        this.builder = builder;
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
        Class<?> wrapperClass = this.builder.bodyWrapperClass;
        return wrapperClass == null ? resultType : ResolvableType.forClassWithGenerics(wrapperClass, resultType);
    }

    private <A, D, W> D doExchange(String url, HttpMethod method, Consumer<HttpHeaders> headersConsumer, ResolvableType resultType, A requestBody, Map<String, ?> uriVariables) {
        if (uriVariables == null) {
            uriVariables = Collections.emptyMap();
        }
        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> consumer = this.builder.defaultHeaders;
        if (headersConsumer != null) {
            consumer = consumer.andThen(headersConsumer);
        }
        consumer.accept(headers);
        long startTime = System.currentTimeMillis();
        ResponseEntity<W> response;
        try {
            if (log.isDebugEnabled()) {
                log.debug("inputParam={}", JacksonUtils.toJson(requestBody));
            }
            response = this.builder.restTemplate.exchange(url, method, new HttpEntity<>(requestBody, headers),
                    ParameterizedTypeReference.forType(resultClass(resultType).getType()), uriVariables);
            if (log.isDebugEnabled()) {
                log.debug("url={}, cost time={}ms", url, System.currentTimeMillis() - startTime);
            }
        } catch (Exception ex) {
            log.error("url={}, cost time={}ms, inputParam={}, errorInfo={}", url, System.currentTimeMillis() - startTime, JacksonUtils.toJson(requestBody), ex.getMessage());
            throw ex;
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("url={}, cost time={}ms, inputParam={}, statusCode={}", url, System.currentTimeMillis() - startTime,
                    JacksonUtils.toJson(requestBody), response.getStatusCode());
            throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR);
        }
        return processResponse(response, url, requestBody);
    }

    private <A, D, W> D processResponse(ResponseEntity<W> responseEntity, String url, A requestBody) {
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
    private <D, W> D extractData(W body) {
        if (this.builder.bodyConverter != null) {
            return (D) this.builder.bodyConverter.apply(body);
        }
        D result = (D) body;
        if (this.builder.bodyWrapperClass == RestResp.class) {
            RestResp<D> restResp = (RestResp<D>) body;
            if (restResp.isFailed() && this.builder.failFast) {
                throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR, restResp.getErrorInfo());
            }
            result = restResp.getData();
        }
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RestClient create() {
        return builder().build();
    }

    public static RestClient defaults() {
        return builder().bodyWrapperClass(RestResp.class).build();
    }

    public static class Builder {

        private Class<?> bodyWrapperClass;
        private Function<Object, Object> bodyConverter;
        private RestTemplate restTemplate;
        private Consumer<HttpHeaders> defaultHeaders = headers -> {};
        private boolean failFast = true;

        private Builder() {
        }

        public Builder bodyWrapperClass(Class<?> bodyWrapperClass) {
            this.bodyWrapperClass = bodyWrapperClass;
            return this;
        }

        public Builder bodyConverter(Function<Object, Object> bodyConverter) {
            this.bodyConverter = bodyConverter;
            return this;
        }

        public Builder restTemplate(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
            return this;
        }

        public Builder defaultHeaders(Consumer<HttpHeaders> defaultHeaders) {
            this.defaultHeaders = this.defaultHeaders.andThen(defaultHeaders);
            return this;
        }

        public Builder failFast(boolean failFast) {
            this.failFast = failFast;
            return this;
        }

        public RestClient build() {
            if (this.restTemplate == null) {
                this.restTemplate = new RestTemplate();
                for (HttpMessageConverter<?> messageConverter : this.restTemplate.getMessageConverters()) {
                    if (messageConverter instanceof MappingJackson2HttpMessageConverter jackson) {
                        SimpleModule module = new SimpleModule();
                        module.addDeserializer(JsonObject.class, new JsonObject.JsonObjectDeserializer());
                        module.addDeserializer(JsonArray.class, new JsonArray.JsonArrayDeserializer());
                        jackson.getObjectMapper().registerModule(module);
                        break;
                    }
                }
            }
            return new RestClient(this);
        }

    }

}
