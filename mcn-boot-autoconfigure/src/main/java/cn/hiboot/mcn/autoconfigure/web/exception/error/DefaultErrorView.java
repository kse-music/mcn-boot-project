package cn.hiboot.mcn.autoconfigure.web.exception.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        String blueprint = "/blueprint.png";
        String errorHanger = "/error-hanger.png";
        String errorPin = "/error-pin.png";
        if(StringUtils.hasText(basePath)){
            blueprint = basePath + blueprint;
            errorHanger = basePath + errorHanger;
            errorPin = basePath + errorPin;
        }
        String status = model.get("status").toString();
        Object message = model.get("message");
        if(message == null){
            message = model.get("error");
        }
        String msg = message == null ? "" : message.toString();
        String view = StreamUtils.copyToString(getClass().getClassLoader().getResourceAsStream("defaultErrorView.html"),StandardCharsets.UTF_8);
        view = view.replace("{blueprint}",blueprint)
                .replace("{errorHanger}",errorHanger)
                .replace("{errorPin}",errorPin)
                .replace("{status}",htmlEscape(status))
                .replace("{msg}",htmlEscape(msg));
        response.getWriter().append(view);
    }

    private String htmlEscape(Object input) {
        return (input != null) ? HtmlUtils.htmlEscape(input.toString()) : null;
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
