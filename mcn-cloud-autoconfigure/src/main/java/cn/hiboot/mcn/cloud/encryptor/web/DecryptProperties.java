package cn.hiboot.mcn.cloud.encryptor.web;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DecryptProperties
 *
 * @author DingHao
 * @since 2022/8/4 21:32
 */
@ConfigurationProperties("mcn.decrypt")
public class DecryptProperties extends NameValueProcessorProperties {
    /**
     * 过滤器名
     */
    private String name = "defaultDecryptFilter";

    private int order = Integer.MIN_VALUE + 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
