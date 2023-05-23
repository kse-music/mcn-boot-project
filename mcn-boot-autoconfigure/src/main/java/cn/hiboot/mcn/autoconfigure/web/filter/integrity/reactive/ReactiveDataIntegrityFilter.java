package cn.hiboot.mcn.autoconfigure.web.filter.integrity.reactive;

import cn.hiboot.mcn.autoconfigure.web.filter.common.JsonRequestHelper;
import cn.hiboot.mcn.autoconfigure.web.filter.common.RequestMatcher;
import cn.hiboot.mcn.autoconfigure.web.filter.integrity.DataIntegrityException;
import cn.hiboot.mcn.autoconfigure.web.filter.integrity.DataIntegrityProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.integrity.DataIntegrityUtils;
import cn.hiboot.mcn.autoconfigure.web.reactor.ServerHttpResponseUtils;
import cn.hiboot.mcn.core.tuples.Pair;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * ReactiveDataIntegrityFilter
 *
 * @author DingHao
 * @since 2022/6/4 23:41
 */
public class ReactiveDataIntegrityFilter implements OrderedWebFilter {
    private final Logger log = LoggerFactory.getLogger(ReactiveDataIntegrityFilter.class);

    private final DataIntegrityProperties dataIntegrityProperties;
    private final RequestMatcher requestMatcher;

    public ReactiveDataIntegrityFilter(DataIntegrityProperties dataIntegrityProperties) {
        this.dataIntegrityProperties = dataIntegrityProperties;
        this.requestMatcher = new RequestMatcher(dataIntegrityProperties.getIncludePatterns(), dataIntegrityProperties.getExcludePatterns()).enableDefaultExclude();
    }

    private String getHeader(ServerHttpRequest request, String headerName) {
        return request.getHeaders().getFirst(headerName);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.just(exchange.getRequest()).filter(requestMatcher::matches).flatMap(request -> {
            String tsm = getHeader(request, "TSM");// 获取时间戳
            if (tsm == null) {
                tsm = getHeader(request, "timestamp");
            }
            String timestamp = tsm;
            if (StrUtil.isEmpty(timestamp)) {
                return Mono.error(DataIntegrityException.newInstance("验证失败,无效的时间戳"));
            }
            if (dataIntegrityProperties.isCheckReplay()) {
                long receiveTime = Long.parseLong(timestamp);
                long NONCE_STR_TIMEOUT_SECONDS = dataIntegrityProperties.getTimeout().toMillis();// 判断时间是否大于 1 分钟 (防止重放攻击)
                if (System.currentTimeMillis() - receiveTime > NONCE_STR_TIMEOUT_SECONDS) {
                    return Mono.error(DataIntegrityException.newInstance("验证失败,无效的时间戳"));
                }
            }
            String signature = getHeader(request, "signature");// 获取签名
            if (StrUtil.isEmpty(signature)) {
                return Mono.error(DataIntegrityException.newInstance("验证失败,数据被篡改"));
            }
            String nonceStr = getHeader(request, "nonceStr");// 获取随机字符串

            return exchange.getFormData().map(formParams -> {
                Map<String, Object> params = new HashMap<>();
                exchange.getRequest().getQueryParams().forEach((name, value) -> params.put(name, value.get(0)));
                formParams.forEach((name, value) -> params.put(name, value.get(0)));
                return params;
            }).flatMap(params -> parseUpload(exchange).map(fileInfo -> Pair.with(params,fileInfo))).flatMap(p -> {
                if (request.getMethod() == HttpMethod.POST && MediaType.APPLICATION_JSON.isCompatibleWith(request.getHeaders().getContentType())) {
                    ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return super.getBody().flatMap(dataBuffer -> {
                                String payload = JsonRequestHelper.getData(dataBuffer.asInputStream());
                                if (!Objects.equals(signature, DataIntegrityUtils.signature(timestamp, nonceStr, p.getValue0(), null,payload))) {
                                    return Mono.error(DataIntegrityException.newInstance("验证失败,数据被篡改"));
                                }
                                return Flux.just(exchange.getResponse().bufferFactory().wrap(payload.getBytes(StandardCharsets.UTF_8)));
                            });
                        }
                    };
                    return chain.filter(exchange.mutate().request(decorator).build());
                }
                if (!Objects.equals(signature, DataIntegrityUtils.signature(timestamp, nonceStr, p.getValue0(), p.getValue1(),null))) {
                    return Mono.error(DataIntegrityException.newInstance("验证失败,数据被篡改"));
                }
                return Mono.empty();
            });
        }).switchIfEmpty(chain.filter(exchange)).onErrorResume(DataIntegrityException.class,ex -> {
            log.error("Check DataIntegrity Failed: {}", ex.getMessage());
            return ServerHttpResponseUtils.failed(ex.getMessage(), exchange.getResponse());
        });
    }

    private Mono<String> parseUpload(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(request.getHeaders().getContentType()) && dataIntegrityProperties.isCheckUpload()) {//maybe upload
            return exchange.getMultipartData().map(m -> {
                StringBuilder str = new StringBuilder();
                m.forEach((k,v) -> {
                    for (Part part : v) {
                        part.content().map(DataBuffer::capacity).subscribe(l -> str.append(getSubmittedFileName(part)).append(l));
                    }
                });
                return str.toString();
            });
        }
        return Mono.just("");
    }

    public String getSubmittedFileName(Part part) {
        String fileName = null;
        String cd =  part.headers().getFirst("Content-Disposition");
        if (cd != null) {
            String cdl = cd.toLowerCase(Locale.ENGLISH);
            if (cdl.startsWith("form-data") || cdl.startsWith("attachment")) {
                String[] split = cdl.split(";");
                for (String s : split) {
                    s = s.trim();
                    if (s.startsWith("filename")) {
                        fileName = s.split("=")[1].replace("\"","");
                    }
                }
            }
        }
        if(fileName == null){
            return "";
        }
        if (fileName.startsWith("=?") && fileName.endsWith("?=")) {
            fileName = URLDecoder.decode(fileName,StandardCharsets.UTF_8);
        }
        return fileName;
    }

    @Override
    public int getOrder() {
        return dataIntegrityProperties.getOrder();
    }


}
