package cn.hiboot.mcn.autoconfigure.web.reactor;

import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * ServerHttpResponseUtils
 *
 * @author DingHao
 * @since 2022/5/23 23:41
 */
public abstract class ServerHttpResponseUtils {

    public static Mono<Void> success(ServerHttpResponse response) {
        return write(new RestResp<>(),response);
    }

    public static Mono<Void> success(Object data, ServerHttpResponse response) {
        return write(new RestResp<>(data),response);
    }

    public static Mono<Void> failed(Throwable e, ServerHttpResponse response) {
        return failed(e.getMessage(),response);
    }

    public static Mono<Void> failed(String msg, ServerHttpResponse response) {
        return write(RestResp.error(msg),response);
    }

    private static Mono<Void> write(RestResp<?> resp,ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(JacksonUtils.toJson(resp).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

}