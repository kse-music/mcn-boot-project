package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.web.exception.error.GlobalExceptionViewResolver;
import cn.hiboot.mcn.core.config.McnConstant;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
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
    private GlobalExceptionViewResolver viewResolver;
    private boolean setValidatorResult;
    private boolean removeFrameworkStack;
    private boolean overrideHttpError;
    private String basePackage;

    @ExceptionHandler(Throwable.class)
    public Object handleException(HttpServletRequest request, Throwable exception) throws Throwable{
        if(viewResolver != null && viewResolver.support(request)){
            logError(exception);
            return viewResolver.view(request, exception);
        }
        return buildErrorData(request, exception);
    }

    protected abstract RestResp<Object> buildErrorData(HttpServletRequest request,Throwable exception) throws Throwable;

    protected RestResp<Object> buildErrorMessage(Integer code,Object data,Throwable t){
        return buildErrorMessage(code, null,data, t);
    }

    protected RestResp<Object> buildErrorMessage(Integer code,String msg,Object data,Throwable t){
        if(removeFrameworkStack){//移除异常栈中非业务应用包下的栈信息
            dealCurrentStackTraceElement(t);
        }
        //打印异常栈
        logError(t);
        RestResp<Object> resp = RestResp.error(code, getExceptionMsg(code,msg,t));
        if(data != null && setValidatorResult){//参数校验具体错误数据信息
            resp.setData(data);
        }
        return resp;
    }

    private String getExceptionMsg(Integer code,String msg,Throwable t){
        String exMsg = msg;
        if(ObjectUtils.isEmpty(exMsg)){//acquire msg from exception
            exMsg = t.getMessage();
        }
        if(ObjectUtils.isEmpty(exMsg)){//acquire msg from code
            exMsg = ErrorMsg.getErrorMsg(code);
        }
        if(ObjectUtils.isEmpty(exMsg)){
            log.warn("please set {} exception message",code == BaseException.DEFAULT_CODE?"":"code = "+code);
        }
        return exMsg;
    }

    private void logError(Throwable t){
        log.error("The exception information is as follows",t);
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

    public void setErrorViewResolver(GlobalExceptionViewResolver viewResolver) {
        this.viewResolver = viewResolver;
    }
}
