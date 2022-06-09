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

    /**
     * 排除的url路径
     */
    private List<String> excludeUrls;
    /**
     * 不处理的字段,仅对非json编码有效
     */
    private List<String> excludeFields;
    /**
     * 是否过滤参数名称,仅对非json编码有效
     */
    private boolean filterParameterName;

    private int order = Integer.MAX_VALUE;

    /**
     * 是否也处理返回数据
     */
    private boolean escapeResponse;

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public void setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
    }

    public List<String> getExcludeFields() {
        return excludeFields;
    }

    public void setExcludeFields(List<String> excludeFields) {
        this.excludeFields = excludeFields;
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

    public boolean isEscapeResponse() {
        return escapeResponse;
    }

    public void setEscapeResponse(boolean escapeResponse) {
        this.escapeResponse = escapeResponse;
    }

}
