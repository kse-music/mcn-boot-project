package cn.hiboot.mcn.autoconfigure.web.filter.integrity;

import cn.hiboot.mcn.autoconfigure.web.filter.common.JsonRequestHelper;
import cn.hiboot.mcn.autoconfigure.web.filter.common.RequestMatcher;
import cn.hiboot.mcn.autoconfigure.web.filter.common.servlet.RequestPayloadRequestWrapper;
import cn.hiboot.mcn.autoconfigure.web.mvc.WebUtils;
import cn.hiboot.mcn.core.util.McnUtils;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.*;

/**
 * DataIntegrityFilter
 *
 * @author DingHao
 * @since 2022/6/4 23:41
 */
public class DataIntegrityFilter implements Filter, Ordered {
    private final Logger log = LoggerFactory.getLogger(DataIntegrityFilter.class);

    private final DataIntegrityProperties dataIntegrityProperties;
    private final RequestMatcher requestMatcher;

    public DataIntegrityFilter(DataIntegrityProperties dataIntegrityProperties) {
        this.dataIntegrityProperties = dataIntegrityProperties;
        this.requestMatcher = new RequestMatcher(dataIntegrityProperties.getIncludePatterns(),dataIntegrityProperties.getExcludePatterns()).enableDefaultExclude();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        if(requestMatcher.matches(request)){//需要校验完整性的
            String timestamp = request.getHeader("TSM");// 获取时间戳
            if(timestamp == null){
                timestamp = request.getHeader("timestamp");
            }
            String nonceStr = request.getHeader("nonceStr");// 获取随机字符串
            String signature = request.getHeader("signature");// 获取签名

            if (StrUtil.isEmpty(timestamp)) {
                WebUtils.failed("验证失败,无效的时间戳",(HttpServletResponse) servletResponse);
                return;
            }

            if(dataIntegrityProperties.isCheckReplay()){
                long receiveTime = Long.parseLong(timestamp);
                long NONCE_STR_TIMEOUT_SECONDS = dataIntegrityProperties.getTimeout().toMillis();// 判断时间是否大于 1 分钟 (防止重放攻击)
                if (System.currentTimeMillis() - receiveTime > NONCE_STR_TIMEOUT_SECONDS) {
                    WebUtils.failed("验证失败,时间戳过期",(HttpServletResponse) servletResponse);
                    return;
                }
            }

            String payload = null;
            if(JsonRequestHelper.isJsonRequest(request)){//json请求
                RequestPayloadRequestWrapper wrapper = new RequestPayloadRequestWrapper(request);
                payload = wrapper.getPayload();
                if(!payload.isEmpty()){
                    servletRequest = wrapper;
                }
            }

            if (isInValid(signature, timestamp, nonceStr, request, payload)) {
                WebUtils.failed("验证失败,数据被篡改",(HttpServletResponse)servletResponse);
                return;
            }
        }

        filterChain.doFilter(servletRequest,servletResponse);
    }

    /**
     * 后端生成的 sm3加密编码
     * (通过 参数+时间戳+随机数   生成的编码)
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonceStr 随机数
     * @param request 参数
     * @param payload json请求体
     * @return boolean
     */
    private boolean isInValid(String signature,String timestamp, String nonceStr, HttpServletRequest request,String payload) {
        if(StrUtil.isEmpty(signature)){
            return true;
        }
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
        String sign = DataIntegrityUtils.signature(timestamp, nonceStr, params, fileInfo, payload);
        boolean rs = Objects.equals(signature, sign);
        if(rs){
            return false;
        }
        log.error("kv param = {},payload = {},fileInfo = {},signature = {}",params,payload,fileInfo,sign);
        return true;
    }

    @Override
    public int getOrder() {
        return dataIntegrityProperties.getOrder();
    }

    private String parseUpload(HttpServletRequest request) {
        StringBuilder str = new StringBuilder();
        try {
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                str.append(DataIntegrityUtils.md5UploadFile(McnUtils.copyToByteArray(part.getInputStream()),part.getSubmittedFileName())).append("&");
            }
        }
        catch (Throwable ignored) {
        }
        if (!str.isEmpty()) {
            return str.substring(0, str.length() - 1);
        }
        return str.toString();
    }

}
