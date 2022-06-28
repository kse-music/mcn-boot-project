package cn.hiboot.mcn.autoconfigure.web.filter.integrity;

import cn.hiboot.mcn.autoconfigure.web.filter.common.RequestPayloadRequestWrapper;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * DataIntegrityFilter
 *
 * @author DingHao
 * @since 2022/6/4 23:41
 */
public class DataIntegrityFilter implements Filter, Ordered {

    private final DataIntegrityProperties dataIntegrityProperties;
    private final PathMatcher pathPattern;

    public DataIntegrityFilter(DataIntegrityProperties dataIntegrityProperties) {
        this.dataIntegrityProperties = dataIntegrityProperties;
        this.pathPattern = new AntPathMatcher();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        boolean isMatch = false;
        String path = request.getServletPath();
        for (String excludePattern : dataIntegrityProperties.getExcludePatterns()) {
            isMatch = pathPattern.match(excludePattern,path);
            if (isMatch) {
                break;
            }
        }
        if (isMatch) {//不需要校验完整性的
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        for (String excludePattern : dataIntegrityProperties.getIncludePatterns()) {
            isMatch = pathPattern.match(excludePattern,path);
            if (isMatch) {
                break;
            }
        }

        if(isMatch){//需要校验完整性的
            String timestamp = request.getHeader("TSM");// 获取时间戳
            if(timestamp == null){
                timestamp = request.getHeader("timestamp");
            }
            String nonceStr = request.getHeader("nonceStr");// 获取随机字符串
            String signature = request.getHeader("signature");// 获取签名

            if (StrUtil.isEmpty(timestamp)) {
                write("验证失败,无效的时间戳",(HttpServletResponse) servletResponse);
                return;
            }

            if(dataIntegrityProperties.isCheckReplay()){
                long receiveTime = Long.parseLong(timestamp);
                long NONCE_STR_TIMEOUT_SECONDS = dataIntegrityProperties.getTimeout().toMillis();// 判断时间是否大于 1 分钟 (防止重放攻击)
                if (System.currentTimeMillis() - receiveTime > NONCE_STR_TIMEOUT_SECONDS) {
                    write("验证失败,时间戳过期",(HttpServletResponse) servletResponse);
                    return;
                }
            }

            String currentSignature;
            if(RequestPayloadRequestWrapper.isJsonRequest(request)){//json请求
                String data = null;
                try{
                    data = IoUtil.read(request.getInputStream(),StandardCharsets.UTF_8);
                }catch (Exception e){//兼容
                    //ignore
                }
                if(data != null){
                    servletRequest = new RequestPayloadRequestWrapper(request,data);
                }
                currentSignature = signature(timestamp, nonceStr, request,data);
            }else {
                currentSignature = signature(timestamp, nonceStr, request,null);
            }

            // 对请求头参数进行签名
            if (StrUtil.isEmpty(signature) || !Objects.equals(signature, currentSignature)) {
                write("验证失败,数据被篡改",(HttpServletResponse)servletResponse);
                return;
            }
        }

        filterChain.doFilter(servletRequest,servletResponse);
    }

    private void write(String msg, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.write(JacksonUtils.toJson(RestResp.error(msg)));
        out.flush();
        out.close();
    }

    /**
     * 后端生成的 sm3加密编码
     * (通过 参数+时间戳+随机数   生成的编码)
     * @param timestamp 时间戳
     * @param nonceStr 随机数
     * @param request 参数
     * @return signature
     */
    private String signature(String timestamp, String nonceStr, HttpServletRequest request,String data) {
        Map<String, Object> params = new HashMap<>();
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()){
            String name = enumeration.nextElement();
            String[] parameterValues = request.getParameterValues(name);
            params.put(name, parameterValues[parameterValues.length - 1]);
        }
        String fileInfo = null;
        if(request.getContentType() != null && request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE) && dataIntegrityProperties.isCheckUpload()){//maybe upload
            fileInfo = parseUpload(request);
        }
        return DataIntegrityUtils.signature(timestamp,nonceStr,params,fileInfo,data);
    }

    @Override
    public int getOrder() {
        return dataIntegrityProperties.getOrder();
    }

    private String parseUpload(HttpServletRequest request) {
        String str = "";
        try {
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                String filename = part.getSubmittedFileName();
                if (filename != null) {
                    if (filename.startsWith("=?") && filename.endsWith("?=")) {
                        filename = URLDecoder.decode(filename,StandardCharsets.UTF_8);
                    }
                    str = str.concat(filename + part.getSize());
                }
            }
        }
        catch (Throwable ex) {
            //
        }
        return str;
    }

}
