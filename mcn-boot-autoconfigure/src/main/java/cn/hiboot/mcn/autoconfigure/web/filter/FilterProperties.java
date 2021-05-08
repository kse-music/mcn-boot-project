package cn.hiboot.mcn.autoconfigure.web.filter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/1/9 11:34
 */
@ConfigurationProperties("mcn.filter")
public class FilterProperties {

    /**
     * 排除的url路径
     */
    private List<String> excludes = new ArrayList<>(Arrays.asList("/favicon.ico","/img/*","/js/*","/css/*"));
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
