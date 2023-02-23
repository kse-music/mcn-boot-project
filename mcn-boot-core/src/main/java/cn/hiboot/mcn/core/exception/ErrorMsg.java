package cn.hiboot.mcn.core.exception;

import cn.hiboot.mcn.core.util.McnUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * ErrorMsg
 *
 * @author DingHao
 * @since 2021/11/30 22:29
 */
public abstract class ErrorMsg {

    private static final List<Properties> errMsgProp;

    static {
        errMsgProp = new ArrayList<>(4);
        errMsgProp.add(McnUtils.loadProperties("error-msg.properties"));
        errMsgProp.add(McnUtils.loadProperties("mcn-error-msg.properties",ErrorMsg.class));
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

}
