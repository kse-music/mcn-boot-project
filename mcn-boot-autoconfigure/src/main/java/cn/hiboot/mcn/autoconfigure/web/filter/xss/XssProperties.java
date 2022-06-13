package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessorProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * XssProperties
 *
 * @author DingHao
 * @since 2019/1/9 11:34
 */
@ConfigurationProperties("mcn.xss")
public class XssProperties extends ValueProcessorProperties {

    /**
     * XssFilter过滤器顺序默认Integer.MAX_VALUE
     */
    private int order = Integer.MAX_VALUE;

    /**
     * 是否也处理返回数据
     */
    private boolean escapeResponse;

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
