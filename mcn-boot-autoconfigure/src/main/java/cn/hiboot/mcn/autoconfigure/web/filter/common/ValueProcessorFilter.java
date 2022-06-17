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

    private final ValueProcessor valueProcessor;
    private final ValueProcessorProperties properties;
    private final RequestMatcher requestMatcher;

    public ValueProcessorFilter(ValueProcessorProperties properties,ValueProcessor valueProcessor) {
        this.properties = properties;
        this.valueProcessor = valueProcessor;
        this.requestMatcher = new RequestMatcher(properties);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (requestMatcher.matches(req)) {
            request = new ValueProcessorRequestWrapper(req, properties.getExcludeFields(),properties.isFilterParameterName(),valueProcessor);
        }
        filterChain.doFilter(request, response);
    }

}
