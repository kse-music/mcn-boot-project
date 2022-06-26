package cn.hiboot.mcn.autoconfigure.web.filter.common;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * ValueProcessorFilter
 *
 * @author DingHao
 * @since 2019/1/9 11:11
 */
public class ValueProcessorFilter implements Filter {

    private final List<String> excludeFields;
    private final boolean filterParameterName;

    private final ValueProcessor valueProcessor;
    private final RequestMatcher requestMatcher;

    public ValueProcessorFilter(ValueProcessor valueProcessor) {
        this(null,false,valueProcessor);
    }

    public ValueProcessorFilter(List<String> excludeFields, boolean filterParameterName, ValueProcessor valueProcessor) {
        this.excludeFields = excludeFields;
        this.filterParameterName = filterParameterName;
        this.valueProcessor = valueProcessor;
        this.requestMatcher = valueProcessor.requestMatcher();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (requestMatcher.matches(req)) {
            request = new ValueProcessorRequestWrapper(req,excludeFields,filterParameterName,valueProcessor);
        }
        filterChain.doFilter(request, response);
    }

}
