package cn.hiboot.mcn.autoconfigure.web.filter.common;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

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

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final ValueProcessor valueProcessor;
    private final ValueProcessorProperties properties;

    public ValueProcessorFilter(ValueProcessorProperties properties,ValueProcessor valueProcessor) {
        this.properties = properties;
        this.valueProcessor = valueProcessor;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (isExcludeUrl(req)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (isIncludeUrl(req)) {
            request = new ValueProcessorRequestWrapper(req, properties.getExcludeFields(),properties.isFilterParameterName(),valueProcessor);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isIncludeUrl(HttpServletRequest request) {
        if(CollectionUtils.isEmpty(properties.getIncludeUrls())){
            return true;
        }
        String url = request.getServletPath();
        for (String pattern : properties.getIncludeUrls()) {
            if (antPathMatcher.match(pattern,url)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcludeUrl(HttpServletRequest request) {
        String url = request.getServletPath();
        for (String pattern : properties.getExcludeUrls()) {
            if (antPathMatcher.match(pattern,url)) {
                return true;
            }
        }
        return false;
    }

}
