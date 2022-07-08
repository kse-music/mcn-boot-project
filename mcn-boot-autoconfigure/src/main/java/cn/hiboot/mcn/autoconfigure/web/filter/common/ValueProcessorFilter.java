package cn.hiboot.mcn.autoconfigure.web.filter.common;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * ValueProcessorFilter
 *
 * @author DingHao
 * @since 2019/1/9 11:11
 */
public class ValueProcessorFilter implements Filter {

    private final ValueProcessorProperties properties;
    private final ValueProcessor valueProcessor;
    private final RequestMatcher requestMatcher;

    public ValueProcessorFilter(ValueProcessorProperties properties,ValueProcessor valueProcessor) {
        this.properties = properties;
        this.valueProcessor = valueProcessor;
        this.requestMatcher = valueProcessor.requestMatcher();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (requestMatcher.matches(req)) {
            request = new ValueProcessorRequestWrapper(req,valueProcessor)
                    .filterHeaderValue(properties.isFilterHeaderValue())
                    .filterParameterName(properties.isFilterParameterName())
                    .excludeFields(properties.getExcludeFields());
        }
        filterChain.doFilter(request, response);
    }

}
