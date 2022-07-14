package cn.hiboot.mcn.autoconfigure.web.exception;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.GlobalExceptionProperties;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ValidationErrorBean;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ValidationExceptionHandler;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ExceptionHelper
 *
 * @author DingHao
 * @since 2022/7/14 14:09
 */
public class ExceptionHelper {

    /**
     * 非BaseException的默认code码
     */
    public static final int DEFAULT_ERROR_CODE = 999998;

    private final Logger log;
    private final GlobalExceptionProperties properties;
    private final boolean validationExceptionPresent;
    private final boolean overrideHttpError;
    private final String basePackage;

    public ExceptionHelper(GlobalExceptionProperties properties, Environment environment,Logger log) {
        this.properties = properties;
        this.log = log;
        this.basePackage = environment.getProperty(ConfigProperties.APP_BASE_PACKAGE);
        this.overrideHttpError = environment.getProperty("http.error.override",Boolean.class,true);
        this.validationExceptionPresent = ClassUtils.isPresent("javax.validation.ValidationException", getClass().getClassLoader());
    }

    public void handleError(Error error) {
        if(error instanceof VirtualMachineError){
            GlobalExceptionProperties.JvmError jvm = properties.getJvmError();
            if(jvm != null && jvm.isExit()){
                System.exit(jvm.getStatus());
            }
        }
    }

    public void logError(Throwable t){
        if(properties.isRemoveFrameworkStack()){
            dealCurrentStackTraceElement(t);
        }
        log.error("The exception information is as follows",t);
    }

    private void dealCurrentStackTraceElement(Throwable exception){
        if(Objects.isNull(exception.getCause())){//is self
            return;
        }
        exception.setStackTrace(Arrays.stream(exception.getStackTrace()).filter(s -> s.getClassName().contains(basePackage)).toArray(StackTraceElement[]::new));
    }

    public RestResp<Object> doHandleException(Throwable exception){
        try {
            return doHandleException(null,exception);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    public RestResp<Object> doHandleException(CustomHandler customHandler, Throwable exception) throws Throwable {
        Integer errorCode = DEFAULT_ERROR_CODE;
        List<ValidationErrorBean> data = null;
        if(exception instanceof BaseException){
            errorCode = ((BaseException) exception).getCode();
        }else if(exception instanceof MethodArgumentTypeMismatchException){
            errorCode = ExceptionKeys.PARAM_TYPE_ERROR;
        }else if(exception instanceof MaxUploadSizeExceededException){
            errorCode = ExceptionKeys.UPLOAD_FILE_SIZE_ERROR;
        }else if(exception instanceof BindException){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            BindException ex = (BindException) exception;
            data = dealBindingResult(ex.getBindingResult());
        }else if(validationExceptionPresent && ValidationExceptionHandler.support(exception)){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            data = ValidationExceptionHandler.handle(exception);
        }else if(exception instanceof HttpMessageNotReadableException || exception instanceof InvalidFormatException) {
            errorCode = ExceptionKeys.JSON_PARSE_ERROR;
        }else if(exception instanceof VirtualMachineError){
            errorCode = ExceptionKeys.HTTP_ERROR_500;
            handleError((Error) exception.getCause());
        }else if(customHandler != null){
            Integer error = customHandler.handle(exception);
            if(error != null){
                errorCode = error;
            }
        }
        String errorInfo = null;
        if(properties.isUniformExMsg()){
            if(errorCode == DEFAULT_ERROR_CODE){
                errorCode = ExceptionKeys.SERVICE_ERROR;
            }
            errorInfo = ErrorMsg.getErrorMsg(errorCode);
        }
        return buildErrorMessage(errorCode,errorInfo,data,exception);
    }

    private RestResp<Object> buildErrorMessage(Integer code,String msg,List<ValidationErrorBean> data,Throwable t){
        if(ObjectUtils.isEmpty(msg)){//这里的消息可能是重写后的
            msg = t.getMessage();//1.take msg from exception
            if(ObjectUtils.isEmpty(msg)){
                msg = ErrorMsg.getErrorMsg(code);//2.take msg from code
                if(ObjectUtils.isEmpty(msg) && code != DEFAULT_ERROR_CODE && code != BaseException.DEFAULT_ERROR_CODE){
                    log.warn("please set code = {} exception message", code);//3.log no exception message
                }
            }
        }
        RestResp<Object> resp = RestResp.error(code, msg);
        if(data != null && properties.isReturnValidateResult()){//设置参数校验具体错误数据信息
            resp.setData(data);
        }
        return resp;
    }

    private List<ValidationErrorBean> dealBindingResult(BindingResult bindingResult){
        return bindingResult.getAllErrors().stream().map(e -> {
                    if(e instanceof FieldError){
                        FieldError fieldError = (FieldError) e;
                        return new ValidationErrorBean(e.getDefaultMessage(),fieldError.getField(), fieldError.getRejectedValue() == null ? null : fieldError.getRejectedValue().toString());
                    }
                    return new ValidationErrorBean(e.getDefaultMessage(),e.getObjectName(), null);
                }
        ).collect(Collectors.toList());
    }


    public boolean isOverrideHttpError() {
        return overrideHttpError;
    }

    public interface CustomHandler{
        Integer handle(Throwable ex) throws Throwable;
    }
}
