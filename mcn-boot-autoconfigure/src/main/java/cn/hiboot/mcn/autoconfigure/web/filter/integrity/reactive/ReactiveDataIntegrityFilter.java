package cn.hiboot.mcn.autoconfigure.web.filter.integrity.reactive;

import cn.hiboot.mcn.autoconfigure.web.filter.common.JsonRequestHelper;
import cn.hiboot.mcn.autoconfigure.web.filter.common.RequestMatcher;
import cn.hiboot.mcn.autoconfigure.web.filter.integrity.DataIntegrityProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.integrity.DataIntegrityUtils;
import cn.hiboot.mcn.autoconfigure.web.reactor.ServerHttpResponseUtils;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import org.apache.tomcat.util.http.fileupload.ParameterParser;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
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
public class ReactiveDataIntegrityFilter implements WebFilter, Ordered {

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
                return Mono.error(ServiceException.newInstance("验证失败,无效的时间戳"));
            }

            if (dataIntegrityProperties.isCheckReplay()) {
                long receiveTime = Long.parseLong(timestamp);
                long NONCE_STR_TIMEOUT_SECONDS = dataIntegrityProperties.getTimeout().toMillis();// 判断时间是否大于 1 分钟 (防止重放攻击)
                if (System.currentTimeMillis() - receiveTime > NONCE_STR_TIMEOUT_SECONDS) {
                    return Mono.error(ServiceException.newInstance("验证失败,无效的时间戳"));
                }
            }

            String signature = getHeader(request, "signature");// 获取签名
            if (StrUtil.isEmpty(signature)) {
                return Mono.error(ServiceException.newInstance("验证失败,数据被篡改"));
            }
            String nonceStr = getHeader(request, "nonceStr");// 获取随机字符串
            if (request.getMethod() == HttpMethod.POST && MediaType.APPLICATION_JSON.equals(request.getHeaders().getContentType())) {
                ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return super.getBody().flatMap(dataBuffer -> {
                            String str = JsonRequestHelper.getData(dataBuffer.asInputStream());
                            if (!Objects.equals(signature, signature(timestamp, nonceStr, exchange, str))) {
                                return Mono.error(ServiceException.newInstance("验证失败,数据被篡改"));
                            }
                            return Flux.just(exchange.getResponse().bufferFactory().wrap(str.getBytes(StandardCharsets.UTF_8)));
                        });
                    }
                };
                return chain.filter(exchange.mutate().request(decorator).build());
            }

            // 对请求头参数进行签名
            if (!Objects.equals(signature, signature(timestamp, nonceStr, exchange, null))) {
                return Mono.error(ServiceException.newInstance("验证失败,数据被篡改"));
            }
            return Mono.empty();
        }).switchIfEmpty(chain.filter(exchange)).onErrorResume(e -> ServerHttpResponseUtils.failed(e.getMessage(), exchange.getResponse()));
    }

    /**
     * 后端生成的 sm3加密编码
     * (通过 参数+时间戳+随机数   生成的编码)
     *
     * @param timestamp 时间戳
     * @param nonceStr  随机数
     * @param exchange   参数
     * @param payload   json请求体
     * @return signature
     */
    private String signature(String timestamp, String nonceStr, ServerWebExchange exchange, String payload) {
        Map<String, Object> params = new HashMap<>();
        ServerHttpRequest request = exchange.getRequest();
        request.getQueryParams().forEach((name, value) -> params.put(name, value.get(0)));
        String fileInfo = null;
        if (request.getHeaders().getContentType() != null && request.getHeaders().getContentType().isCompatibleWith(MediaType.MULTIPART_FORM_DATA) && dataIntegrityProperties.isCheckUpload()) {//maybe upload
            fileInfo = parseUpload(exchange);
        }
        return DataIntegrityUtils.signature(timestamp, nonceStr, params, fileInfo, payload);
    }

    private String parseUpload(ServerWebExchange exchange) {
        StringBuilder rs = new StringBuilder();
        exchange.getMultipartData().map(m -> {
            StringBuilder str = new StringBuilder();
            m.forEach((k,v) -> {
                for (Part part : v) {
                    part.content().map(DataBuffer::capacity).subscribe(l -> str.append(getSubmittedFileName(part)).append(l));
                }
            });
            return str.toString();
        }).subscribe(rs::append);
        return rs.toString();
    }

    public String getSubmittedFileName(Part part) {
        String fileName = null;
        String cd =  part.headers().getFirst("Content-Disposition");
        if (cd != null) {
            String cdl = cd.toLowerCase(Locale.ENGLISH);
            if (cdl.startsWith("form-data") || cdl.startsWith("attachment")) {
                ParameterParser paramParser = new ParameterParser();
                paramParser.setLowerCaseNames(true);
                Map<String, String> params = paramParser.parse(cd, ';');
                if (params.containsKey("filename")) {
                    fileName = params.get("filename");
                    if (fileName != null) {
                        if (fileName.indexOf('\\') > -1) {
                            fileName = HttpParser.unquote(fileName.trim());
                        } else {
                            fileName = fileName.trim();
                        }
                    } else {
                        fileName = "";
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
