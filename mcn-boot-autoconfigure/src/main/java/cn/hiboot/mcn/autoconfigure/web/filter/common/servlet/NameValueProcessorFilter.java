package cn.hiboot.mcn.autoconfigure.web.filter.common.servlet;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.common.RequestMatcher;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * NameValueProcessorFilter
 *
 * @author DingHao
 * @since 2019/1/9 11:11
 */
public class NameValueProcessorFilter implements Filter {

    private final NameValueProcessorProperties properties;
    private final NameValueProcessor valueProcessor;
    private final RequestMatcher requestMatcher;

    public NameValueProcessorFilter(NameValueProcessorProperties properties, NameValueProcessor valueProcessor) {
        this.properties = properties;
        this.valueProcessor = valueProcessor;
        this.requestMatcher = new RequestMatcher(properties.getIncludeUrls(), properties.getExcludeUrls()).enableDefaultExclude();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (requestMatcher.matches(req)) {
            request = new NameValueProcessorRequestWrapper(req,valueProcessor)
                    .filterHeaderValue(properties.isFilterHeaderValue())
                    .filterParameterName(properties.isFilterParameterName())
                    .processPayload(properties.isProcessPayload())
                    .excludeFields(properties.getExcludeFields());
        }
        filterChain.doFilter(request, response);
    }

}
