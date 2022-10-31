package cn.hiboot.mcn.autoconfigure.config;

import cn.hiboot.mcn.autoconfigure.web.exception.error.ErrorPageController;
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
    private static final String DATA_SOURCE = "DataSource";

    public static final String APP_BASE_PACKAGE = "app.base-package";

    public static final String MULTIPLE_DATASOURCE_PREFIX = "multiple.datasource";
    public static final String JPA_MULTIPLE_DATASOURCE_PREFIX = "jpa." + MULTIPLE_DATASOURCE_PREFIX;
    public static final String MYBATIS_MULTIPLE_DATASOURCE_PREFIX = "mybatis." + MULTIPLE_DATASOURCE_PREFIX;
    public static final String DYNAMIC_DATASOURCE_PREFIX = "dynamic.datasource";

    private static String error_view;

    static {
        try {
            error_view = StreamUtils.copyToString(createResource("defaultErrorView.html", ErrorPageController.class).getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            //ignore
        }
    }

    public static ClassPathResource mcnDefault() {
        return createResource("mcn-default.properties", ConfigProperties.class);
    }

    public static ClassPathResource createResource(String file,Class<?> clazz) {
        return new ClassPathResource(file,clazz);
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
        return error_view.replace("{blueprint}",blueprint)
                .replace("{errorHanger}",errorHanger)
                .replace("{errorPin}",errorPin)
                .replace("{status}",htmlEscape(status))
                .replace("{msg}",htmlEscape(msg));
    }

    private static String htmlEscape(Object input) {
        return (input != null) ? HtmlUtils.htmlEscape(input.toString()) : null;
    }

    public static String getDataSourceBeanName(String dsName){
        return dsName + DATA_SOURCE;
    }
}
