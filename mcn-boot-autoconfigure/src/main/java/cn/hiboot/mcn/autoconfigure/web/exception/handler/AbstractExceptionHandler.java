package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.core.config.McnConstant;
import cn.hiboot.mcn.core.exception.ErrorMsg;
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
    private boolean overrideHttpError;
    private String basePackage;

    public RestResp<Object> buildErrorMessage(Integer code,Throwable t){
        return buildErrorMessage(code,null,t);
    }

    public RestResp<Object> buildErrorMessage(Integer code,String msg,Throwable t){
        return buildErrorMessage(code,msg,null,t);
    }

    public RestResp<Object> buildErrorMessage(Integer code,String msg,Object data,Throwable t){
        if(ObjectUtils.isEmpty(msg)){//try to acquire msg from code
            msg = ErrorMsg.getErrorMsg(code);
        }
        if(ObjectUtils.isEmpty(msg)){
            log.warn("please set exception message");
        }
        if(removeFrameworkStack){//移除异常栈中非业务应用包下的栈信息
            dealCurrentStackTraceElement(t);
        }
        //打印异常栈
        log.error("The exception information is as follows",t);
        RestResp<Object> resp = new RestResp<>(code, msg);
        if(data != null && setValidatorResult){//参数校验具体错误数据信息
            resp.setData(data);
        }
        return resp;
    }

    private void dealCurrentStackTraceElement(Throwable exception){
        if(Objects.isNull(exception.getCause())){//is self
            return;
        }
        exception.setStackTrace(Arrays.stream(exception.getStackTrace()).filter(s -> s.getClassName().contains(basePackage)).toArray(StackTraceElement[]::new));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.basePackage = environment.getProperty(McnConstant.APP_BASE_PACKAGE);
        this.removeFrameworkStack = environment.getProperty("framework.stack.remove.enable",Boolean.class,true);
        this.setValidatorResult = environment.getProperty("validator.result.return.enable",Boolean.class,true);
        this.overrideHttpError = environment.getProperty("http.error.override",Boolean.class,true);
    }

    protected boolean isOverrideHttpError(){
        return overrideHttpError;
    }

}
