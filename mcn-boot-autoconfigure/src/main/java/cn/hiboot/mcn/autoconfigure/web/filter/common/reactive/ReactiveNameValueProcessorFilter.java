package cn.hiboot.mcn.autoconfigure.web.filter.common.reactive;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.common.RequestMatcher;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.web.server.ServerWebExchange;
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
            NameValueProcessorRequestDecorator decorator = new NameValueProcessorRequestDecorator(exchange,valueProcessor)
                    .filterHeaderValue(properties.isFilterHeaderValue())
                    .filterParameterName(properties.isFilterParameterName())
                    .processPayload(properties.isProcessPayload())
                    .excludeFields(properties.getExcludeFields());
            return exchange.getFormData().flatMap(formData -> Mono.just(decorator.process(formData))).flatMap(p -> chain.filter(exchange.mutate().request(decorator).build()));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return properties.getOrder();
    }

}
