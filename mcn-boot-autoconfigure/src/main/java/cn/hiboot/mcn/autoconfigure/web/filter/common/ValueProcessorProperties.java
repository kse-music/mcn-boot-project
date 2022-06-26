package cn.hiboot.mcn.autoconfigure.web.filter.common;

import java.util.Collections;
import java.util.List;

/**
 * ValueProcessorProperties
 *
 * @author DingHao
 * @since 2022/6/13 12:30
 */
public class ValueProcessorProperties {
    /**
     * 处理的url路径
     */
    private List<String> includeUrls = Collections.singletonList("/**");
    /**
     * 排除的url路径
     */
    private List<String> excludeUrls = Collections.emptyList();;
    /**
     * 不处理的字段,仅对非json编码有效
     */
    private List<String> excludeFields;
    /**
     * 是否过滤参数名称,仅对非json编码有效
     */
    private boolean filterParameterName;

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
}
