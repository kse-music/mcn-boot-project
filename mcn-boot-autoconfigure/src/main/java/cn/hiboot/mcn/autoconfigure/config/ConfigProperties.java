package cn.hiboot.mcn.autoconfigure.config;

import cn.hiboot.mcn.autoconfigure.web.exception.error.ErrorPageController;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map<String, Object> loadConfig(ClassLoader classLoader,String location) {
        if (classLoader == null) {
            classLoader = ConfigProperties.class.getClassLoader();
        }
        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(location);
        }catch (IOException ex) {
            throw new IllegalArgumentException("Failed to load configurations from location [" + location + "]", ex);
        }
        Properties properties = new Properties();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            properties.putAll(McnUtils.loadProperties(url.getFile(), classLoader));
        }
        return (Map) properties;
    }

    public static ClassPathResource mcnDefault() {
        return createResource("mcn-default.properties", ConfigProperties.class);
    }

    public static ClassPathResource createResource(String file,Class<?> clazz) {
        return new ClassPathResource(file,clazz);
    }

    public static String errorView(Map<String, ?> error, String basePath) {
        String status = error.get("status").toString();
        Object message = error.get("message");
        if(McnUtils.isNullOrEmpty(message)){
            message = error.get("error");
        }
        String msg = message == null ? "" : message.toString();
        return error_view.replace("{status}",htmlEscape(status)).replace("{msg}",htmlEscape(msg));
    }

    private static String htmlEscape(Object input) {
        return (input != null) ? HtmlUtils.htmlEscape(input.toString()) : null;
    }

    public static String getDataSourceBeanName(String dsName){
        return dsName + DATA_SOURCE;
    }
}
