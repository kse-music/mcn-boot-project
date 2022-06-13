package cn.hiboot.mcn.autoconfigure.web.filter.special;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * ParamProcessorProperties
 *
 * @author DingHao
 * @since 2022/6/6 15:04
 */
@ConfigurationProperties("param.processor")
public class ParamProcessorProperties {
    private boolean useFilter;

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

    private int order = Integer.MAX_VALUE - 1;

    public boolean isUseFilter() {
        return useFilter;
    }

    public void setUseFilter(boolean useFilter) {
        this.useFilter = useFilter;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
