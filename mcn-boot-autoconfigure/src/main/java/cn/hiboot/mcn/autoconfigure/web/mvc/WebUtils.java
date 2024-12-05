package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * WebUtils
 *
 * @author DingHao
 * @since 2023/1/20 23:27
 */
public abstract class WebUtils {
    private static final String UNKNOWN = "unknown";

    public static String getRemoteAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ObjectUtils.isEmpty(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ObjectUtils.isEmpty(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ObjectUtils.isEmpty(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    public static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null){
            return null;
        }
        return requestAttributes.getRequest();
    }

    public static void requestAttributesIfNonNull(Consumer<ServletRequestAttributes> consumer) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes != null){
            consumer.accept(requestAttributes);
        }
    }

    public static <T> void success(T data, HttpServletResponse response){
        write(RestResp.ok(data),response);
    }

    public static <T> void success(T data,Long count, HttpServletResponse response){
        write(RestResp.ok(data,count),response);
    }

    public static void failed(String msg, HttpServletResponse response){
        write(RestResp.error(msg),response);
    }

    public static void failed(Integer code,HttpServletResponse response){
        write(RestResp.error(code),response);
    }

    public static void failed(Integer code,String msg, HttpServletResponse response){
        write(RestResp.error(code,msg),response);
    }

    public static <T> void write(RestResp<T> resp, HttpServletResponse response){
        write(JacksonUtils.toJson(resp),response);
    }

    public static void write(String str, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);
        try(PrintWriter out = response.getWriter()){
            out.write(str);
            out.flush();
        } catch (IOException ignored) {
        }
    }

}
