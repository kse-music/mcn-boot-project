package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import cn.hiboot.mcn.autoconfigure.web.filter.FilterProperties;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * XssFilter
 *
 * @author DingHao
 * @since 2019/1/9 11:11
 */
public class XssFilter implements Filter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final FilterProperties filterProperties;

    public XssFilter(FilterProperties filterProperties) {
        this.filterProperties = filterProperties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (isExcludeURL(req)) {
            filterChain.doFilter(request, response);
            return;
        }
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request, filterProperties);
        filterChain.doFilter(xssRequest, response);
    }

    private boolean isExcludeURL(HttpServletRequest request) {
        if(CollectionUtils.isEmpty(filterProperties.getExcludes())){
            return false;
        }
        String url = request.getServletPath();
        for (String pattern : filterProperties.getExcludes()) {
            if (antPathMatcher.match(pattern,url)) {
                return true;
            }
        }
        return false;
    }

}
