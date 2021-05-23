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
     * 不拦截的路径
     */
    private String[] excludes;

    public boolean isEnableDefaultIgnore() {
        return enableDefaultIgnore;
    }

    public void setEnableDefaultIgnore(boolean enableDefaultIgnore) {
        this.enableDefaultIgnore = enableDefaultIgnore;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }
}
