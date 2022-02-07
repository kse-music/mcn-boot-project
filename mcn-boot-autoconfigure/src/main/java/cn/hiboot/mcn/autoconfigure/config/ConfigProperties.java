package cn.hiboot.mcn.autoconfigure.config;

import org.springframework.core.io.ClassPathResource;

/**
 * 定位mcn默认配置所在位置
 *
 * @author DingHao
 * @since 2020/7/22 23:57
 */
public abstract class ConfigProperties {

    public static final String APP_BASE_PACKAGE = "app.base-package";
    public static final String DEFAULT_PROPERTY_SOURCE_NAME = "mcn-default";

    public static ClassPathResource mcnDefault() {
        return createResource("mcn-default.properties");
    }

    private static ClassPathResource createResource(String file) {
        return new ClassPathResource(file, ConfigProperties.class);
    }

}
