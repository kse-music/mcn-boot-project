package cn.hiboot.mcn.autoconfigure.web.filter.special;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * ParamProcessorProperties
 *
 * @author DingHao
 * @since 2022/6/6 15:04
 */
@ConfigurationProperties("param.processor")
public class ParamProcessorProperties extends NameValueProcessorProperties implements EnvironmentAware {
    public static String globalRule = "";
    /**
     * 过滤器名
     */
    private String name = "defaultParamProcessorFilter";

    /**
     * 使用过滤器处理参数
     */
    private boolean useFilter;

    /**
     * 参数处理过滤器顺序默认Integer.MAX_VALUE - 1
     */
    private int order = Integer.MAX_VALUE - 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUseFilter() {
        return useFilter;
    }

    public void setUseFilter(boolean useFilter) {
        this.useFilter = useFilter;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void setEnvironment(Environment environment) {
        globalRule = environment.getProperty("global.rule.pattern", "");
    }
}
