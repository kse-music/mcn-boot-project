package cn.hiboot.mcn.autoconfigure.web.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSecurityProperties
 *
 * @author DingHao
 * @since 2021/5/23 23:46
 */
@ConfigurationProperties("web.security")
public class WebSecurityProperties {

    /**
     * 启用默认忽略路径,默认启用
     */
    private boolean enableDefaultIgnore = true;

    /**
     * 默认不拦截的路径,不经过安全过滤器链
     */
    private String[] defaultExcludeUrls;

    /**
     * 不拦截的路径,不经过安全过滤器链
     */
    private String[] excludeUrls;

    private int order = -101;

    public boolean isEnableDefaultIgnore() {
        return enableDefaultIgnore;
    }

    public void setEnableDefaultIgnore(boolean enableDefaultIgnore) {
        this.enableDefaultIgnore = enableDefaultIgnore;
    }

    public String[] getDefaultExcludeUrls() {
        return defaultExcludeUrls;
    }

    public void setDefaultExcludeUrls(String[] defaultExcludeUrls) {
        this.defaultExcludeUrls = defaultExcludeUrls;
    }

    public String[] getExcludeUrls() {
        return excludeUrls;
    }

    public void setExcludeUrls(String[] excludeUrls) {
        this.excludeUrls = excludeUrls;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
