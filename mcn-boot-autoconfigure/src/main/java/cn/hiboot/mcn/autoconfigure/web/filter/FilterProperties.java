package cn.hiboot.mcn.autoconfigure.web.filter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/1/9 11:34
 */
@ConfigurationProperties("mcn.xss.filter")
public class FilterProperties {

    public static final List<String> DEFAULT_EXCLUDE_URL = Arrays.asList("/favicon.ico","/img/*","/js/*","/css/*");

    /**
     * 排除的url路径
     */
    private List<String> excludes;
    /**
     * 是否过滤富文本内容
     */
    private boolean includeRichText = true;

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public boolean isIncludeRichText() {
        return includeRichText;
    }

    public void setIncludeRichText(boolean includeRichText) {
        this.includeRichText = includeRichText;
    }

}
