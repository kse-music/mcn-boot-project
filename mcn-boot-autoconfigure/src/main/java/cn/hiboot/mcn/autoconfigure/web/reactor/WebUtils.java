package cn.hiboot.mcn.autoconfigure.web.reactor;

import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * WebUtils
 *
 * @author DingHao
 * @since 2022/5/23 23:41
 */
public abstract class WebUtils {

    private static final String UNKNOWN = "unknown";

    public static String getRemoteAddr(ServerHttpRequest request) {
        String ipAddress = getHeader(request,"X-Forwarded-For");
        if (ObjectUtils.isEmpty(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = getHeader(request,"Proxy-Client-IP");
        }
        if (ObjectUtils.isEmpty(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = getHeader(request,"WL-Proxy-Client-IP");
        }
        if (ObjectUtils.isEmpty(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            InetSocketAddress remoteAddress = request.getRemoteAddress();
            if(remoteAddress != null){
                ipAddress = remoteAddress.getAddress().getHostAddress();
            }
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    public static String getHeader(ServerHttpRequest request, String headerName) {
        return request.getHeaders().getFirst(headerName);
    }

    public static Mono<Void> success(ServerHttpResponse response) {
        return write(new RestResp<>(),response);
    }

    public static Mono<Void> success(Object data, ServerHttpResponse response) {
        return write(RestResp.ok(data),response);
    }

    public static Mono<Void> failed(Throwable e, ServerHttpResponse response) {
        return failed(e.getMessage(),response);
    }

    public static Mono<Void> failed(String msg, ServerHttpResponse response) {
        return write(RestResp.error(msg),response);
    }

    public static Mono<Void> failed(Integer code, ServerHttpResponse response) {
        return write(RestResp.error(code),response);
    }

    public static Mono<Void> write(RestResp<?> resp,ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.OK);
        return write(JacksonUtils.toJson(resp),response);
    }

    public static Mono<Void> write(String msg,ServerHttpResponse response) {
        if(msg == null){
            msg = "";
        }
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(msg.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

}
