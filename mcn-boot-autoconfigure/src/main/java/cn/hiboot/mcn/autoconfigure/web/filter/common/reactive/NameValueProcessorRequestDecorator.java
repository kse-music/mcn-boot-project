package cn.hiboot.mcn.autoconfigure.web.filter.common.reactive;

import cn.hiboot.mcn.autoconfigure.web.filter.common.JsonRequestHelper;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NameValueProcessorRequestDecorator
 *
 * @author DingHao
 * @since 2023/5/23 11:36
 */
class NameValueProcessorRequestDecorator extends ServerHttpRequestDecorator {

    private List<String> excludeFields;
    private boolean filterParameterName;
    private boolean processPayload;
    private boolean filterHeaderValue;
    private final NameValueProcessor valueProcessor;
    private final ServerWebExchange exchange;

    public NameValueProcessorRequestDecorator(ServerWebExchange exchange, NameValueProcessor valueProcessor) {
        super(exchange.getRequest());
        this.exchange = exchange;
        this.valueProcessor = valueProcessor;
    }

    public NameValueProcessorRequestDecorator excludeFields(List<String> excludeFields) {
        this.excludeFields = excludeFields;
        return this;
    }

    public NameValueProcessorRequestDecorator filterParameterName(boolean filterParameterName) {
        this.filterParameterName = filterParameterName;
        return this;
    }

    public NameValueProcessorRequestDecorator filterHeaderValue(boolean filterHeaderValue) {
        this.filterHeaderValue = filterHeaderValue;
        return this;
    }

    public NameValueProcessorRequestDecorator processPayload(boolean processPayload) {
        this.processPayload = processPayload;
        return this;
    }

    @Override
    public MultiValueMap<String, String> getQueryParams() {
        return process(super.getQueryParams());
    }

    MultiValueMap<String, String> process(MultiValueMap<String, String> data) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (McnUtils.isNotNullAndEmpty(data)) {
            for (String key : data.keySet()) {
                String currentKey = key;
                key = cleanParameterName(currentKey);
                List<String> values = data.get(currentKey);
                if(isExcludeParameter(currentKey)){
                    map.put(currentKey, values);
                    continue;
                }
                List<String> rs = new ArrayList<>();
                for (String value : values) {
                    if (McnUtils.isNotNullAndEmpty(value)) {
                        value = cleanParameterValue(currentKey,value);
                    }
                    rs.add(value);
                }
                map.put(key, Collections.unmodifiableList(rs));
            }
        }
        return map;
    }

    @Override
    public HttpHeaders getHeaders() {
        if(filterHeaderValue){
            return new HttpHeaders(process(super.getHeaders()));
        }
        return super.getHeaders();
    }

    @Override
    public Flux<DataBuffer> getBody() {
        if(processPayload){
            return super.getBody().flatMap(dataBuffer -> {
                String data = JsonRequestHelper.getData(dataBuffer.asInputStream());
                data = clean(null,data);
                return Flux.just(exchange.getResponse().bufferFactory().wrap(data.getBytes(StandardCharsets.UTF_8)));
            });
        }
        return super.getBody();
    }

    private boolean isExcludeParameter(String name){
        if(CollectionUtils.isEmpty(excludeFields)){
            return false;
        }
        return excludeFields.contains(name);
    }

    private String cleanParameterName(String name){
        if(filterParameterName){
            return clean(name,name);
        }
        return name;
    }

    private String cleanParameterValue(String name,String value){
        return clean(name,value);
    }

    private String clean(String name,String text){
        return valueProcessor.process(name,text);
    }
}
