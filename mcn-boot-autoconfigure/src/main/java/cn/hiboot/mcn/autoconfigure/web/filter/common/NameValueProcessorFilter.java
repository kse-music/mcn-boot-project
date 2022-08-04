package cn.hiboot.mcn.autoconfigure.web.filter.common;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
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
        this.requestMatcher = new RequestMatcher(properties.getIncludeUrls(), properties.getExcludeUrls());
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
