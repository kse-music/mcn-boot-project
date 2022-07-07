package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionMessageProcessor;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionPostProcessor;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.error.GlobalExceptionViewResolver;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.NestedServletException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
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

    /**
     * 非BaseException的默认code码
     */
    public static final int DEFAULT_ERROR_CODE = 999998;

    private final GlobalExceptionProperties properties;

    private boolean validationExceptionPresent;
    private boolean overrideHttpError;
    private String basePackage;

    private final GlobalExceptionViewResolver viewResolver;
    private final ExceptionPostProcessor<?> exceptionPostProcessor;
    private final List<ExceptionResolver> exceptionResolvers;
    private final ExceptionMessageProcessor exceptionMessageProcessor;

    public GlobalExceptionHandler(GlobalExceptionProperties properties,
                                  ObjectProvider<ExceptionPostProcessor<?>> exceptionPostProcessors,
                                  ObjectProvider<ExceptionResolver> exceptionResolver,
                                  ObjectProvider<ExceptionMessageProcessor> exceptionMessageProcessor,
                                  ObjectProvider<GlobalExceptionViewResolver> globalExceptionViewResolvers) {
        this.properties = properties;
        this.exceptionPostProcessor = exceptionPostProcessors.getIfUnique();
        this.exceptionResolvers = exceptionResolver.orderedStream().collect(Collectors.toList());
        this.exceptionMessageProcessor = exceptionMessageProcessor.getIfUnique();
        this.viewResolver = globalExceptionViewResolvers.getIfUnique();
    }

    @ExceptionHandler(Throwable.class)
    public Object handleException(HttpServletRequest request, Throwable exception) throws Throwable{
        if(Objects.nonNull(viewResolver) && viewResolver.support(request)){
            logError(exception);
            return viewResolver.view(request, exception);
        }
        if(Objects.nonNull(exceptionPostProcessor)){
            Object o = exceptionPostProcessor.beforeHandle(request, exception);
            if(Objects.nonNull(o)){
                logError(exception);
                return o;
            }
        }
        RestResp<Object> resp = null;
        for (ExceptionResolver resolver : exceptionResolvers) {
            if(resolver.support(request, exception)){
                RestResp<Object> rs = resolver.resolveException(request, exception);
                if(Objects.isNull(rs)){
                    continue;
                }
                resp = rs;
            }
        }
        if(Objects.isNull(resp)){
            resp = doHandleException(request, exception);
        }
        if(properties.isOverrideExMsg() && exceptionMessageProcessor != null){
            String message = exceptionMessageProcessor.process(resp.getErrorCode());
            if(Objects.nonNull(message)){
                resp.setErrorInfo(message);
            }
        }
        logError(exception);
        if(Objects.nonNull(exceptionPostProcessor)){
            Object o = exceptionPostProcessor.afterHandle(request, exception, resp);
            if(Objects.nonNull(o)){
                return o;
            }
        }
        return resp;
    }

    protected RestResp<Object> doHandleException(HttpServletRequest request, Throwable exception) throws Throwable {
        Integer errorCode = DEFAULT_ERROR_CODE;
        List<ValidationErrorBean> data = null;
        if(exception instanceof BaseException){
            errorCode = ((BaseException) exception).getCode();
        }else if(exception instanceof MethodArgumentTypeMismatchException){
            errorCode = ExceptionKeys.PARAM_TYPE_ERROR;
        }else if(exception instanceof MaxUploadSizeExceededException){
            errorCode = ExceptionKeys.UPLOAD_FILE_SIZE_ERROR;
        }else if(exception instanceof ServletRequestBindingException || exception instanceof BindException || exception instanceof MethodArgumentNotValidException){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            if(exception instanceof BindException){
                BindException ex = (BindException) exception;
                data = dealBindingResult(ex.getBindingResult());
            }else if(exception instanceof MethodArgumentNotValidException){
                MethodArgumentNotValidException ex = (MethodArgumentNotValidException) exception;
                data = dealBindingResult(ex.getBindingResult());
            }
        }else if(validationExceptionPresent && ValidationExceptionHandler.support(exception)){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            data = ValidationExceptionHandler.handle(exception);
        }else if(exception instanceof ServletException){
            errorCode =  mappingCode(((ServletException) exception));
            if (exception instanceof NestedServletException && exception.getCause() instanceof Error) {
                handleError((Error) exception.getCause());
            }
        }else if(exception instanceof HttpMessageNotReadableException || exception instanceof InvalidFormatException) {
            errorCode = ExceptionKeys.JSON_PARSE_ERROR;
        }else if(exception instanceof VirtualMachineError){
            errorCode = ExceptionKeys.HTTP_ERROR_500;
            handleError((Error) exception.getCause());
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

    private void handleError(Error error) {
        if(error instanceof VirtualMachineError){
            GlobalExceptionProperties.JvmError jvm = properties.getJvmError();
            if(jvm != null && jvm.isExit()){
                System.exit(jvm.getStatus());
            }
        }
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

    private void logError(Throwable t){
        if(properties.isRemoveFrameworkStack()){//移除异常栈中非业务应用包下的栈信息
            dealCurrentStackTraceElement(t);
        }
        log.error("The exception information is as follows",t);
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

    private void dealCurrentStackTraceElement(Throwable exception){
        if(Objects.isNull(exception.getCause())){//is self
            return;
        }
        exception.setStackTrace(Arrays.stream(exception.getStackTrace()).filter(s -> s.getClassName().contains(basePackage)).toArray(StackTraceElement[]::new));
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
    public void setEnvironment(Environment environment) {
        this.basePackage = environment.getProperty(ConfigProperties.APP_BASE_PACKAGE);
        this.overrideHttpError = environment.getProperty("http.error.override",Boolean.class,true);
        this.validationExceptionPresent = ClassUtils.isPresent("javax.validation.ValidationException", getClass().getClassLoader());
    }

    @Override
    public int getOrder() {
        return properties.getOrder();
    }

}
