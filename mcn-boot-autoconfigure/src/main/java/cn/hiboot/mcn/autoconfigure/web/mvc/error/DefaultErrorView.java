package cn.hiboot.mcn.autoconfigure.web.mvc.error;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * DefaultErrorView
 *
 * @author DingHao
 * @since 2021/11/24 15:10
 */
public class DefaultErrorView implements View {

    private final ServerProperties serverProperties;

    public DefaultErrorView(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String basePath = serverProperties.getServlet().getContextPath();
        String blueprint = "/blueprint.png";
        String errorHanger = "/error-hanger.png";
        String errorPin = "/error-pin.png";
        if(StringUtils.hasText(basePath) && !basePath.equals("/")){
            if(basePath.endsWith("/")){
                basePath = basePath.substring(0,basePath.length()-2);
            }
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
        String view = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />\n" +
                "    <style>\n" +
                "        html,\n" +
                "        body {\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "\n" +
                "        body {\n" +
                "            background-color: #f2f2f2;\n" +
                "            color: #444;\n" +
                "            font: 12px/1.5 'Helvetica Neue', Arial, Helvetica, sans-serif;\n" +
                "            background: url(\"{blueprint}\") repeat 0 0;\n" +
                "        }\n" +
                "\n" +
                "        div.da-wrapper {\n" +
                "            width: 100%;\n" +
                "            height: auto;\n" +
                "            min-height: 100%;\n" +
                "            position: relative;\n" +
                "            min-width: 320px\n" +
                "        }\n" +
                "\n" +
                "        div.da-wrapper .da-container {\n" +
                "            width: 96%;\n" +
                "            margin: auto\n" +
                "        }\n" +
                "\n" +
                "        div.da-content {\n" +
                "            clear: both;\n" +
                "            padding-bottom: 58px\n" +
                "        }\n" +
                "\n" +
                "        @media only screen and (max-width:480px) {\n" +
                "            div.da-content {\n" +
                "                margin-top: auto\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        div.da-error-wrapper {\n" +
                "            width: 320px;\n" +
                "            padding: 60px 0;\n" +
                "            margin: auto;\n" +
                "            position: relative\n" +
                "        }\n" +
                "\n" +
                "        div.da-error-wrapper .da-error-heading {\n" +
                "            color: #e15656;\n" +
                "            text-align: center;\n" +
                "            font-size: 24px;\n" +
                "            font-family: Georgia, \"Times New Roman\", Times, serif\n" +
                "        }\n" +
                "\n" +
                "        @-webkit-keyframes error-swing {\n" +
                "            0% {\n" +
                "                -webkit-transform: rotate(1deg)\n" +
                "            }\n" +
                "\n" +
                "            100% {\n" +
                "                -webkit-transform: rotate(-2deg)\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        @-moz-keyframes error-swing {\n" +
                "            0% {\n" +
                "                -moz-transform: rotate(1deg)\n" +
                "            }\n" +
                "\n" +
                "            100% {\n" +
                "                -moz-transform: rotate(-2deg)\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        @keyframes error-swing {\n" +
                "            0% {\n" +
                "                transform: rotate(1deg)\n" +
                "            }\n" +
                "\n" +
                "            100% {\n" +
                "                transform: rotate(-2deg)\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        div.da-error-wrapper .da-error-code {\n" +
                "            width: 285px;\n" +
                "            height: 170px;\n" +
                "            padding: 127px 16px 0 16px;\n" +
                "            position: relative;\n" +
                "            margin: auto;\n" +
                "            margin-bottom: 20px;\n" +
                "            z-index: 5;\n" +
                "            line-height: 1;\n" +
                "            font-size: 32px;\n" +
                "            text-align: center;\n" +
                "            background: url(\"{errorHanger}\") no-repeat center center;\n" +
                "            -webkit-transform-origin: center top;\n" +
                "            -moz-transform-origin: center top;\n" +
                "            transform-origin: center top;\n" +
                "            -webkit-animation: error-swing infinite 2s ease-in-out alternate;\n" +
                "            -moz-animation: error-swing infinite 2s ease-in-out alternate;\n" +
                "            animation: error-swing infinite 2s ease-in-out alternate\n" +
                "        }\n" +
                "\n" +
                "        div.da-error-wrapper .da-error-code .tip {\n" +
                "            padding-top: 20px;\n" +
                "            color: #e15656;\n" +
                "        }\n" +
                "\n" +
                "        div.da-error-wrapper .da-error-code .tip2 {\n" +
                "            padding-top: 15px;\n" +
                "        }\n" +
                "\n" +
                "        div.da-error-wrapper .da-error-code .tip3 {\n" +
                "            padding-top: 20px;\n" +
                "            font-size: 16px;\n" +
                "            color: #e15656;\n" +
                "        }\n" +
                "\n" +
                "        div.da-error-wrapper .da-error-pin {\n" +
                "            width: 38px;\n" +
                "            height: 38px;\n" +
                "            display: block;\n" +
                "            margin: auto;\n" +
                "            margin-bottom: -27px;\n" +
                "            z-index: 10;\n" +
                "            position: relative;\n" +
                "            background: url(\"{errorPin}\") no-repeat center center\n" +
                "        }\n" +
                "\n" +
                "        p {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "    </style>\n" +
                "    <title>错误页面</title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "<div class=\"da-wrapper\">\n" +
                "    <div class=\"da-content\">\n" +
                "        <div class=\"da-container clearfix\">\n" +
                "            <div class=\"da-error-wrapper\">\n" +
                "                <div class=\"da-error-pin\"></div>\n" +
                "                <div class=\"da-error-code\">\n" +
                "                    <p class=\"tip\">STATUS:{status}</p>\n" +
                "<!--                     <p class=\"tip2\">服务器开小差了</p>-->\n" +
                "                    <p class=\"tip3\">{msg}</p>\n" +
                "                </div>\n" +
                "<!--                <h1 class=\"da-error-heading\">Sorry, 请稍后再试 !!!</h1>-->\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
        view = view.replace("{blueprint}",blueprint)
                .replace("{errorHanger}",errorHanger)
                .replace("{errorPin}",errorPin)
                .replace("{status}",htmlEscape(status))
                .replace("{msg}",htmlEscape(msg));
        response.setCharacterEncoding("UTF-8");
        response.getWriter().append(view);
    }

    private String htmlEscape(Object input) {
        return (input != null) ? HtmlUtils.htmlEscape(input.toString()) : null;
    }

    @Override
    public String getContentType() {
        return "text/html";
    }
}
