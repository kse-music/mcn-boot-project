package cn.hiboot.mcn.autoconfigure.web.filter.common;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * RequestMatcher
 *
 * @author DingHao
 * @since 2022/6/17 17:06
 */
public class RequestMatcher {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final ValueProcessorProperties properties;

    public RequestMatcher(ValueProcessorProperties properties) {
        this.properties = properties;
    }

    public boolean matches(HttpServletRequest request){
        String url = request.getServletPath();
        if (isExcludeUrl(url)) {
            return false;
        }
        return isIncludeUrl(url);
    }

    private boolean isIncludeUrl(String url) {
        return CollectionUtils.isEmpty(properties.getIncludeUrls()) || doMatch(url,properties.getIncludeUrls());
    }

    private boolean isExcludeUrl(String url) {
        return doMatch(url,properties.getExcludeUrls());
    }

    private boolean doMatch(String url, List<String> urls) {
        for (String pattern : urls) {
            if (antPathMatcher.match(pattern,url)) {
                return true;
            }
        }
        return false;
    }
}
