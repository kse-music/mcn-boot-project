package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionMessageCustomizer;
import cn.hiboot.mcn.autoconfigure.web.exception.error.GlobalExceptionViewResolver;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * global exception handler
 *
 * @author DingHao
 * @since 2021/5/8 17:27
 */
@RestControllerAdvice
public class GlobalExceptionHandler implements EnvironmentAware, Ordered {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final int DEFAULT_ERROR_CODE = BaseException.DEFAULT_ERROR_CODE;

    private GlobalExceptionViewResolver viewResolver;
    private boolean setValidatorResult;
    private boolean removeFrameworkStack;
    private boolean overrideHttpError;
    private int order;
    private boolean validationExceptionPresent;
    private String basePackage;

    private final ObjectProvider<ExceptionMessageCustomizer> exceptionHandlers;

    public GlobalExceptionHandler(ObjectProvider<ExceptionMessageCustomizer> exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
    }

    @Autowired(required = false)
    public void setErrorView(GlobalExceptionViewResolver exceptionViewResolver) {
        this.viewResolver = exceptionViewResolver;
    }

    @ExceptionHandler(Throwable.class)
    public Object handleException(HttpServletRequest request, Throwable exception) throws Throwable{
        if(viewResolver != null && viewResolver.support(request)){
            logError(exception);
            return viewResolver.view(request, exception);
        }
        return buildErrorData(request, exception);
    }

    public RestResp<Object> buildErrorData(HttpServletRequest request, Throwable exception) throws Throwable {
        Integer errorCode = null;
        Object data = null;
        if(exception instanceof BaseException){
            errorCode = ((BaseException) exception).getCode();
        }else if(exception instanceof MethodArgumentTypeMismatchException || exception instanceof ServletRequestBindingException
                || exception instanceof BindException || validationExceptionPresent){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            if(exception instanceof BindException){
                BindException ex = (BindException) exception;
                data = dealBindingResult(ex.getBindingResult());
            }else if(validationExceptionPresent){
                data = ValidationExceptionHandler.handle(exception);
            }
        }else if(exception instanceof ServletException){
            errorCode = mappingCode((ServletException) exception);
        }
        ExceptionMessageCustomizer exceptionHandler = exceptionHandlers.getIfUnique();
        if(Objects.nonNull(exceptionHandler)){
            return buildErrorMessage(errorCode,exceptionHandler.handle(exception),data,exception);
        }
        return buildErrorMessage(errorCode,data,exception);
    }

    private Object dealBindingResult(BindingResult bindingResult){
        return bindingResult.getAllErrors().stream().map(e -> {
            if(e instanceof FieldError){
                FieldError fieldError = (FieldError) e;
                return new ValidationErrorBean(e.getDefaultMessage(),fieldError.getField(), fieldError.getRejectedValue() == null ? null : fieldError.getRejectedValue().toString());
            }
            return new ValidationErrorBean(e.getDefaultMessage(),e.getObjectName(), null);
           }
        ).collect(Collectors.toList());
    }

    private RestResp<Object> buildErrorMessage(Integer code,Object data,Throwable t){
        return buildErrorMessage(code, null,data, t);
    }

    private RestResp<Object> buildErrorMessage(Integer code,String msg,Object data,Throwable t){
        if(code == null){
            code = DEFAULT_ERROR_CODE;
        }
        if(removeFrameworkStack){//移除异常栈中非业务应用包下的栈信息
            dealCurrentStackTraceElement(t);
        }
        logError(t);//打印异常栈
        if(ObjectUtils.isEmpty(msg)){//这里的消息可能是重写后的
            msg = t.getMessage();//1.take msg from exception
            if(ObjectUtils.isEmpty(msg)){
                msg = ErrorMsg.getErrorMsg(code);//2.take msg from code
                if(ObjectUtils.isEmpty(msg) && code != DEFAULT_ERROR_CODE){
                    log.warn("please set code = {} exception message", code);//3.log no exception message
                }
            }
        }
        RestResp<Object> resp = RestResp.error(code, msg);
        if(data != null && setValidatorResult){//设置参数校验具体错误数据信息
            resp.setData(data);
        }
        return resp;
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
        this.basePackage = environment.getProperty(ConfigProperties.APP_BASE_PACKAGE);
        this.removeFrameworkStack = environment.getProperty("framework.stack.remove.enable",Boolean.class,true);
        this.setValidatorResult = environment.getProperty("validator.result.return.enable",Boolean.class,true);
        this.overrideHttpError = environment.getProperty("http.error.override",Boolean.class,true);
        this.order = environment.getProperty("global.ex.handler.order",Integer.class,1);
        this.validationExceptionPresent = ClassUtils.isPresent("javax.validation.ValidationException", getClass().getClassLoader());
    }

    private int mappingCode(ServletException exception) throws ServletException {
        if(overrideHttpError){
            int code = ExceptionKeys.HTTP_ERROR_500;
            if (exception instanceof NoHandlerFoundException) {
                code = ExceptionKeys.HTTP_ERROR_404;
            } else if (exception instanceof HttpRequestMethodNotSupportedException) {
                code = ExceptionKeys.HTTP_ERROR_405;
            } else if (exception instanceof HttpMediaTypeException) {
                code = ExceptionKeys.HTTP_ERROR_406;
            } else if (exception instanceof UnavailableException) {
                code = ExceptionKeys.HTTP_ERROR_503;
            }
            return code;
        }
        throw exception;
    }

    @Override
    public int getOrder() {
        return order;
    }

}
