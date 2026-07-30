package cn.hiboot.mcn.autoconfigure.web.exception.error;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * ErrorViewReader
 *
 * @author DingHao
 * @since 2026/7/30 15:29
 */
public class ErrorViewReader {

    private static String error_view;

    static {
        try {
            error_view = StreamUtils.copyToString(ConfigProperties.createResource("defaultErrorView.html", ErrorViewReader.class).getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
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

}
