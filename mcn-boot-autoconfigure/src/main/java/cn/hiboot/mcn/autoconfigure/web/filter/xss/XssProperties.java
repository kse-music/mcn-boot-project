package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * XssProperties
 *
 * @author DingHao
 * @since 2019/1/9 11:34
 */
@ConfigurationProperties("mcn.xss")
public class XssProperties {

    private String[] urlPatterns = {"/*"};

    /**
     * 排除的url路径
     */
    private List<String> excludes;
    /**
     * 是否过滤富文本内容
     * 默认过滤富文本
     */
    private boolean filterRichText = true;
    /**
     * 是否过滤参数名称
     */
    private boolean filterParameterName;

    private int order = Integer.MAX_VALUE;

    public String[] getUrlPatterns() {
        return urlPatterns;
    }

    public void setUrlPatterns(String[] urlPatterns) {
        this.urlPatterns = urlPatterns;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public boolean isFilterRichText() {
        return filterRichText;
    }

    public void setFilterRichText(boolean filterRichText) {
        this.filterRichText = filterRichText;
    }

    public boolean isFilterParameterName() {
        return filterParameterName;
    }

    public void setFilterParameterName(boolean filterParameterName) {
        this.filterParameterName = filterParameterName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
