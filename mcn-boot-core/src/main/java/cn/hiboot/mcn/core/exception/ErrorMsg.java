package cn.hiboot.mcn.core.exception;

import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class ErrorMsg implements ExceptionKeys {

    private static final List<Properties> errMsgProp;

    static {
        errMsgProp = new ArrayList<>();
        errMsgProp.add(McnUtils.loadProperties("mcn-error-msg.properties"));
        errMsgProp.add(McnUtils.loadProperties("error-msg.properties"));
    }

    public static String getErrorMsg(Integer code){
        for (Properties prop : errMsgProp) {
            String propertyValue = prop.getProperty(code.toString());
            if(propertyValue != null){
                return propertyValue;
            }
        }
        return "";
    }

    public static RestResp<Object> invalidCertificate(){
        return buildErrorMessage(ExceptionKeys.INVALID_CERTIFICATE_ERROR);
    }

    public static RestResp<Object> buildErrorMessage(Integer code){
        return buildErrorMessage(code,getErrorMsg(code));
    }

    public static RestResp<Object> buildErrorMessage(Integer code,String msg){
        return new RestResp<>(code, msg);
    }

}
