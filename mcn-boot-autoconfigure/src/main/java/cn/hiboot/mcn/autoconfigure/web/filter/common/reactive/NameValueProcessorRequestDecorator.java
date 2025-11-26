package cn.hiboot.mcn.autoconfigure.web.filter.common.reactive;

import cn.hiboot.mcn.autoconfigure.web.filter.common.JsonRequestHelper;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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

    public NameValueProcessorRequestDecorator(ServerHttpRequest request, NameValueProcessor valueProcessor) {
        super(request);
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
        if (McnUtils.isNullOrEmpty(data)) {
            return new LinkedMultiValueMap<>();
        }

        return process(
                data.keySet(),
                data::get
        );
    }

    private MultiValueMap<String, String> process(HttpHeaders headers) {
        if (McnUtils.isNullOrEmpty(headers)) {
            return new LinkedMultiValueMap<>();
        }

        return process(
                headers.headerNames(),
                headers::get
        );
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
                return Flux.just(dataBuffer.factory().wrap(data.getBytes(StandardCharsets.UTF_8)));
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

    private MultiValueMap<String, String> process(Iterable<String> keys, Function<String, List<String>> valueGetter) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (String k : keys) {
            String currentKey = k;
            String cleanedKey = cleanParameterName(currentKey);
            List<String> values = valueGetter.apply(currentKey);

            if (isExcludeParameter(currentKey)) {
                map.put(currentKey, values);
                continue;
            }

            List<String> cleanedValues = new ArrayList<>();
            for (String value : values) {
                if (McnUtils.isNotNullAndEmpty(value)) {
                    value = cleanParameterValue(currentKey, value);
                }
                cleanedValues.add(value);
            }

            map.put(cleanedKey, Collections.unmodifiableList(cleanedValues));
        }

        return map;
    }

}
