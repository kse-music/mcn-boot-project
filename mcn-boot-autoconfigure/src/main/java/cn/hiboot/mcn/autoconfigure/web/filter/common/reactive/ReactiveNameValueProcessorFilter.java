package cn.hiboot.mcn.autoconfigure.web.filter.common.reactive;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.common.RequestMatcher;
import org.springframework.boot.webflux.filter.OrderedWebFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * ReactiveNameValueProcessorFilter
 *
 * @author DingHao
 * @since 2023/5/23 11:30
 */
public class ReactiveNameValueProcessorFilter implements OrderedWebFilter {

    private final NameValueProcessorProperties properties;
    private final NameValueProcessor valueProcessor;
    private final RequestMatcher requestMatcher;

    public ReactiveNameValueProcessorFilter(NameValueProcessorProperties properties, NameValueProcessor valueProcessor) {
        this.properties = properties;
        this.valueProcessor = valueProcessor;
        this.requestMatcher = new RequestMatcher(properties.getIncludeUrls(), properties.getExcludeUrls()).enableDefaultExclude();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (requestMatcher.matches(exchange.getRequest())) {
            NameValueProcessorRequestDecorator request = new NameValueProcessorRequestDecorator(exchange.getRequest(),valueProcessor)
                    .filterHeaderValue(properties.isFilterHeaderValue())
                    .filterParameterName(properties.isFilterParameterName())
                    .processPayload(properties.isProcessPayload())
                    .excludeFields(properties.getExcludeFields());
            exchange = new ServerWebExchangeDecorator(exchange) {

                @Override
                public ServerHttpRequest getRequest() {
                    return request;
                }

                @Override
                public Mono<MultiValueMap<String, String>> getFormData() {
                    return super.getFormData().map(request::process);
                }

            };
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return properties.getOrder();
    }

}
