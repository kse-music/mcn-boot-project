package cn.hiboot.mcn.autoconfigure.web.filter.integrity;

import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
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
        for (String excludePattern : dataIntegrityProperties.getExcludePatterns()) {
            isMatch = pathPattern.match(excludePattern,request.getRequestURI());
            if (isMatch) {
                break;
            }
        }
        if (isMatch) {//不需要校验完整性的
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        for (String excludePattern : dataIntegrityProperties.getIncludePatterns()) {
            isMatch = pathPattern.match(excludePattern,request.getRequestURI());
            if (isMatch) {
                break;
            }
        }

        if(isMatch){//需要校验完整性的
            String timestamp = request.getHeader("TSM");// 获取时间戳
            String nonceStr = request.getHeader("nonceStr");// 获取随机字符串
            String signature = request.getHeader("signature");// 获取签名

            if (StrUtil.isEmpty(timestamp)) {
                write("验证失败,无效的时间戳",(HttpServletResponse) servletResponse);
                return;
            }

            if(dataIntegrityProperties.isCheckReplay()){
                long receiveTime = Long.parseLong(timestamp);
                long NONCE_STR_TIMEOUT_SECONDS = 1L;// 判断时间是否大于 1 分钟 (防止重放攻击)
                if ((System.currentTimeMillis() - receiveTime) / (1000 * 60) > NONCE_STR_TIMEOUT_SECONDS) {
                    write("验证失败,时间戳过期",(HttpServletResponse) servletResponse);
                }
            }

            String currentSignature;
            String contentType = request.getContentType();
            if(contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)){//json请求
                String data = null;
                try{
                    data = IoUtil.read(request.getInputStream(),StandardCharsets.UTF_8);
                }catch (Exception e){//兼容
                    //ignore
                }
                if(data != null){
                    servletRequest = new DataIntegrityRequestWrapper(request,data);
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
            params.put(name, parameterValues[parameterValues.length-1]);
        }
        //获取参数，因为有的接口是没有参数的，所以要单独处理下
        String param = this.sortQueryParamString(params);
        if(request.getContentType() != null && request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE) && dataIntegrityProperties.isCheckUpload()){//maybe upload
            param += parseUpload(request);
        }
        if(data != null){
            param += data;
        }
        String qs ;
        if(StrUtil.isNotEmpty(param)){
            qs=String.format("%s&timestamp=%s&nonceStr=%s", param, timestamp, nonceStr);
        }else{
            qs=String.format("timestamp=%s&nonceStr=%s", timestamp, nonceStr);
        }
        return SmUtil.sm3(qs);//从前端获取的nonce和后端的 进行对比，如果不一致则表示数据被篡改

    }

    /**
     * 按照字母顺序进行升序排序
     *
     * @param params 请求参数
     * @return 排序后结果
     */
    private String sortQueryParamString(Map<String, Object> params) {
        List<String> listKeys = new ArrayList<>(params.keySet());
        Collections.sort(listKeys);
        StrBuilder content = StrBuilder.create();
        for (String param : listKeys) {
            if(param.equals("signature")){ //如果是全部作为参数传过来，也会接收到加密的签，所以需要过滤掉（下载和导出功能会出现）
                continue;
            }
            Object obj = params.get(param);
            if(obj instanceof Collection || obj instanceof Map){
                obj = JacksonUtils.toJson(obj);
            }
            content.append(param).append("=").append(obj).append("&");
        }
        if (content.length() > 0) {
            return content.subString(0, content.length() - 1);
        }
        return content.toString();
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
