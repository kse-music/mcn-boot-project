package cn.hiboot.mcn.autoconfigure.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 定位mcn默认配置所在位置
 *
 * @author DingHao
 * @since 2020/7/22 23:57
 */
public abstract class ConfigProperties {

    public static final String APP_BASE_PACKAGE = "app.base-package";
    public static final String DEFAULT_PROPERTY_SOURCE_NAME = "mcn-default";

    public static final String MULTIPLE_DATASOURCE_PREFIX = "multiple.datasource";
    public static final String JPA_MULTIPLE_DATASOURCE_PREFIX = "jpa." + MULTIPLE_DATASOURCE_PREFIX;
    public static final String MYBATIS_MULTIPLE_DATASOURCE_PREFIX = "mybatis." + MULTIPLE_DATASOURCE_PREFIX;
    public static final String DYNAMIC_DATASOURCE_PREFIX = "dynamic.datasource";

    public static ClassPathResource mcnDefault() {
        return createResource("mcn-default.properties");
    }

    private static ClassPathResource createResource(String file) {
        return new ClassPathResource(file, ConfigProperties.class);
    }

    public static String errorView(Map<String, ?> error, String basePath) {
        String blueprint = "/blueprint.png";
        String errorHanger = "/error-hanger.png";
        String errorPin = "/error-pin.png";
        if(StringUtils.hasText(basePath)){
            blueprint = basePath + blueprint;
            errorHanger = basePath + errorHanger;
            errorPin = basePath + errorPin;
        }
        String status = error.get("status").toString();
        Object message = error.get("message");
        if(message == null){
            message = error.get("error");
        }
        String msg = message == null ? "" : message.toString();
        String view = "";
        try {
            view = StreamUtils.copyToString(ConfigProperties.class.getClassLoader().getResourceAsStream("defaultErrorView.html"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            //ignore
        }
        return view.replace("{blueprint}",blueprint)
                .replace("{errorHanger}",errorHanger)
                .replace("{errorPin}",errorPin)
                .replace("{status}",htmlEscape(status))
                .replace("{msg}",htmlEscape(msg));
    }

    private static String htmlEscape(Object input) {
        return (input != null) ? HtmlUtils.htmlEscape(input.toString()) : null;
    }

}
