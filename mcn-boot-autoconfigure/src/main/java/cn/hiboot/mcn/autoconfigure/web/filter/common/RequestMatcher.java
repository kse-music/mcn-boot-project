package cn.hiboot.mcn.autoconfigure.web.filter.common;

import cn.hiboot.mcn.autoconfigure.web.security.WebSecurityProperties;
import cn.hiboot.mcn.core.util.McnAssert;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RequestMatcher
 *
 * @author DingHao
 * @since 2022/6/17 17:06
 */
public class RequestMatcher {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private List<String> includeUrls = Collections.singletonList("/**");
    private List<String> excludeUrls = Collections.emptyList();

    private RequestMatcher() {
    }

    public RequestMatcher(List<String> includeUrls, List<String> excludeUrls) {
        McnAssert.notNull(includeUrls,"includeUrls must be not null");
        McnAssert.notNull(excludeUrls,"includeUrls must be not null");
        this.includeUrls = includeUrls;
        this.excludeUrls = excludeUrls;
    }

    public RequestMatcher enableDefaultExclude(){
        WebSecurityProperties webSecurityProperties = SpringBeanUtils.getBean(WebSecurityProperties.class);
        if(webSecurityProperties.isEnableDefaultIgnore()){
            List<String> excludePatterns = new ArrayList<>(excludeUrls);
            Collections.addAll(excludePatterns,webSecurityProperties.getDefaultExcludeUrls());
            this.excludeUrls = excludePatterns;
        }
        return this;
    }

    public boolean matches(HttpServletRequest request){
        String url = request.getServletPath();
        if (doMatch(url,excludeUrls)) {
            return false;
        }
        return doMatch(url,includeUrls);
    }

    public boolean matches(ServerHttpRequest request){
        String url = request.getPath().pathWithinApplication().value();
        if (doMatch(url,excludeUrls)) {
            return false;
        }
        return doMatch(url,includeUrls);
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
