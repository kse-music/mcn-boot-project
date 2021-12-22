package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.context.McnPropertiesPostProcessor;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * AbstractExceptionHandler
 *
 * @author DingHao
 * @since 2019/1/2 19:04
 */
public abstract class AbstractExceptionHandler implements EnvironmentAware {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private boolean setValidatorResult;
    private boolean removeFrameworkStack;
    private String basePackage;

    public RestResp<Object> buildErrorMessage(Integer code,Throwable t){
        return buildErrorMessage(code,null,t);
    }

    public RestResp<Object> buildErrorMessage(Integer code,String msg,Throwable t){
        return buildErrorMessage(code,msg,null,t);
    }

    public RestResp<Object> buildErrorMessage(Integer code,String msg,Object data,Throwable t){
        if(ObjectUtils.isEmpty(msg)){//如果没有错误信息则尝试从错误码中获取错误信息
            msg = ErrorMsg.getErrorMsg(code);
        }
        if(ObjectUtils.isEmpty(msg)){//如果任然无异常信息,fallback服务端内部错误并更改错误码
            code = ExceptionKeys.SERVICE_ERROR;
            msg = ErrorMsg.getErrorMsg(code);
        }
        if(removeFrameworkStack){//移除异常栈中非业务应用包下的栈信息
            dealCurrentStackTraceElement(t);
        }
        logError(msg,t);//打印异常栈
        RestResp<Object> resp = new RestResp<>(code, msg);
        if(data != null && setValidatorResult){//参数校验具体错误数据信息
            resp.setData(data);
        }
        return resp;
    }

    private void logError(String msg,Throwable t){
        log.error("ErrorMsg = {}",msg,t);
    }

    private void dealCurrentStackTraceElement(Throwable exception){
        if(Objects.isNull(exception.getCause())){//is self
            return;
        }
        exception.setStackTrace(Arrays.stream(exception.getStackTrace()).filter(s -> s.getClassName().contains(basePackage)).toArray(StackTraceElement[]::new));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.basePackage = environment.getProperty(McnPropertiesPostProcessor.APP_BASE_PACKAGE);
        this.removeFrameworkStack = environment.getProperty("framework.stack.remove.enable",Boolean.class,true);
        this.setValidatorResult = environment.getProperty("validator.result.return.enable",Boolean.class,true);
    }

}
