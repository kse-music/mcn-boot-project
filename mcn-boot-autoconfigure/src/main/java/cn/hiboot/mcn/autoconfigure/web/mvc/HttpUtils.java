package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * HttpUtils
 *
 * @author DingHao
 * @since 2023/1/20 23:27
 */
public class HttpUtils {

    public static <T> void success(T data, HttpServletResponse response) throws IOException {
        write(JacksonUtils.toJson(new RestResp<>(data)),response);
    }

    public static void failed(String msg, HttpServletResponse response) throws IOException {
        write(JacksonUtils.toJson(RestResp.error(msg)),response);
    }

    public static void write(String str, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.write(str);
        out.flush();
        out.close();
    }

}
