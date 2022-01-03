package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import cn.hiboot.mcn.autoconfigure.web.filter.FilterProperties;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * XssFilter
 *
 * @author DingHao
 * @since 2019/1/9 11:11
 */
public class XssFilter implements Filter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final FilterProperties filterProperties;
    private final List<String> excludes;

    public XssFilter(FilterProperties filterProperties) {
        this.filterProperties = filterProperties;
        List<String> excludes = filterProperties.getExcludes();
        if (McnUtils.isNullOrEmpty(excludes)) {
            excludes = FilterProperties.DEFAULT_EXCLUDE_URL;
        }else {
            excludes.addAll(FilterProperties.DEFAULT_EXCLUDE_URL);
        }
        this.excludes = excludes;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (isExcludeURL(req)) {
            filterChain.doFilter(request, response);
            return;
        }

        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request, filterProperties.isIncludeRichText());
        filterChain.doFilter(xssRequest, response);
    }

    private boolean isExcludeURL(HttpServletRequest request) {
        String url = request.getServletPath();
        for (String pattern : excludes) {
            if (antPathMatcher.match(pattern,url)) {
                return true;
            }
        }
        return false;
    }

}
