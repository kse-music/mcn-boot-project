package cn.hiboot.mcn.core.exception;

import cn.hiboot.mcn.core.util.McnUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * ErrorMsg
 *
 * @author DingHao
 * @since 2021/11/30 22:29
 */
public abstract class ErrorMsg {

    private static final Map<Integer,String> errMsg;

    static {
        errMsg = new HashMap<>();
        loadProperties("error-msg.properties",null);
        loadProperties("mcn-error-msg.properties",ErrorMsg.class);
    }

    private static void loadProperties(String fileName,Class<?> clazz){
        McnUtils.loadProperties(fileName,clazz).forEach((k,v) -> errMsg.put(Integer.parseInt(k.toString()),v.toString()));
    }

    public static String getErrorMsg(Integer code){
        return errMsg.getOrDefault(code,"");
    }

}
