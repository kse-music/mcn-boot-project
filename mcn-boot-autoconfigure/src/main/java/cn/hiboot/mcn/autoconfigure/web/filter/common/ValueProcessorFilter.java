package cn.hiboot.mcn.autoconfigure.web.filter.common;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

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

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final List<String> excludeUrls;
    private final List<String> excludeFields;
    private final boolean filterParameterName;
    private final ValueProcessor valueProcessor;

    public ValueProcessorFilter(List<String> excludeUrls,
                                List<String> excludeFields,
                                boolean filterParameterName,
                                ValueProcessor valueProcessor) {
        this.excludeUrls = excludeUrls;
        this.excludeFields = excludeFields;
        this.filterParameterName = filterParameterName;
        this.valueProcessor = valueProcessor;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (isExcludeURL(req)) {
            filterChain.doFilter(request, response);
            return;
        }
        ValueProcessorRequestWrapper xssRequest = new ValueProcessorRequestWrapper((HttpServletRequest) request, excludeFields,filterParameterName,valueProcessor);
        filterChain.doFilter(xssRequest, response);
    }

    private boolean isExcludeURL(HttpServletRequest request) {
        if(CollectionUtils.isEmpty(excludeUrls)){
            return false;
        }
        String url = request.getServletPath();
        for (String pattern : excludeUrls) {
            if (antPathMatcher.match(pattern,url)) {
                return true;
            }
        }
        return false;
    }

}
