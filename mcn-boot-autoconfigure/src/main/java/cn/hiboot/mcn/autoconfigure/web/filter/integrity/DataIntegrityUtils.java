package cn.hiboot.mcn.autoconfigure.web.filter.integrity;

import cn.hiboot.mcn.core.tuples.Triplet;
import cn.hiboot.mcn.core.util.JacksonUtils;
import cn.hiboot.mcn.core.util.McnUtils;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * DataIntegrityUtils
 *
 * @author DingHao
 * @since 2022/6/21 13:22
 */
public abstract class DataIntegrityUtils {

    public static Triplet<String,String,String> signature(Map<String, Object> params, String payload) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonceStr = McnUtils.simpleUUID().substring(0,8);
        String signature = signature(timestamp, nonceStr, params, null,payload);
        return Triplet.with(timestamp,nonceStr,signature);
    }

    public static String signature(String timestamp, String nonceStr, Map<String, Object> params, String fileInfo, String payload) {
        String param = sortQueryParamString(params);
        if(fileInfo != null){
            param += fileInfo;
        }
        if(StringUtils.hasText(payload)){
            param += payload;
        }
        String qs ;
        if(StrUtil.isNotEmpty(param)){
            qs=String.format("%s&timestamp=%s&nonceStr=%s", param, timestamp, nonceStr);
        }else{
            qs=String.format("timestamp=%s&nonceStr=%s", timestamp, nonceStr);
        }
        return SmUtil.sm3(qs);
    }

    private static String sortQueryParamString(Map<String, Object> params) {
        List<String> listKeys = new ArrayList<>(params.keySet());
        Collections.sort(listKeys);
        StrBuilder content = StrBuilder.create();
        for (String param : listKeys) {
            if(param.equals("signature")){
                continue;
            }
            Object obj = params.get(param);
            if(obj instanceof Collection || obj instanceof Map){
                obj = JacksonUtils.toJson(obj);
            }
            content.append(param).append("=").append(obj).append("&");
        }
        if (!content.isEmpty()) {
            return content.subString(0, content.length() - 1);
        }
        return content.toString();
    }

    public static String md5UploadFile(byte[] bytes,String filename){
        if (filename != null) {
            if (filename.startsWith("=?") && filename.endsWith("?=")) {
                filename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
            }
        }
        String md5 = DigestUtil.md5Hex(bytes);
        return filename + "=" + md5;
    }
}
