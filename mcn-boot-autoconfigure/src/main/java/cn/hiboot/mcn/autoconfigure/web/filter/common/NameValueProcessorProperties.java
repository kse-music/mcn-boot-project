package cn.hiboot.mcn.autoconfigure.web.filter.common;

import java.util.Collections;
import java.util.List;

/**
 * NameValueProcessorProperties
 *
 * @author DingHao
 * @since 2022/6/13 12:30
 */
public abstract class NameValueProcessorProperties {
    /**
     * 处理的url路径
     */
    private List<String> includeUrls = Collections.singletonList("/**");
    /**
     * 排除的url路径
     */
    private List<String> excludeUrls = Collections.emptyList();
    /**
     * 不处理的字段
     */
    private List<String> excludeFields;
    /**
     * 是否处理字段名称,仅对kv参数生效
     */
    private boolean filterParameterName;
    /**
     * 是否过滤header字段值
     */
    private boolean filterHeaderValue;
    /**
     * 是否处理请求体
     */
    private boolean processPayload;

    public List<String> getIncludeUrls() {
        return includeUrls;
    }

    public void setIncludeUrls(List<String> includeUrls) {
        this.includeUrls = includeUrls;
    }

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

    public boolean isFilterHeaderValue() {
        return filterHeaderValue;
    }

    public void setFilterHeaderValue(boolean filterHeaderValue) {
        this.filterHeaderValue = filterHeaderValue;
    }

    public boolean isProcessPayload() {
        return processPayload;
    }

    public void setProcessPayload(boolean processPayload) {
        this.processPayload = processPayload;
    }
}
