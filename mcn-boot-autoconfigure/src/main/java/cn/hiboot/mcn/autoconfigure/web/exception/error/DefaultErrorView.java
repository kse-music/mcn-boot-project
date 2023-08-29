package cn.hiboot.mcn.autoconfigure.web.exception.error;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * DefaultErrorView
 *
 * @author DingHao
 * @since 2021/11/24 15:10
 */
public class DefaultErrorView implements View {

    private final Logger log = LoggerFactory.getLogger(DefaultErrorView.class);

    private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);

    private final ServerProperties serverProperties;

    public DefaultErrorView(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (response.isCommitted()) {
            String message = getMessage(model);
            log.error(message);
            return;
        }
        response.setContentType(TEXT_HTML_UTF8.toString());
        String basePath = serverProperties.getServlet().getContextPath();
        response.getWriter().append(ConfigProperties.errorView(model,basePath));
    }

    private String getMessage(Map<String, ?> model) {
        Object path = model.get("path");
        String message = "Cannot render error page for request [" + path + "]";
        if (model.get("message") != null) {
            message += " and exception [" + model.get("message") + "]";
        }
        message += " as the response has already been committed.";
        message += " As a result, the response may have the wrong status code.";
        return message;
    }

    @Override
    public String getContentType() {
        return "text/html";
    }
}
