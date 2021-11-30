package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.web.exception.handler.AbstractExceptionHandler;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ExceptionMessageCustomizer;
import cn.hiboot.mcn.core.exception.JsonException;
import cn.hiboot.mcn.core.model.ValidationErrorBean;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * global exception config
 *
 * @author DingHao
 * @since 2021/5/8 17:27
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractExceptionHandler {

    private final ObjectProvider<ExceptionMessageCustomizer<?>> exceptionHandlers;

    public GlobalExceptionHandler(ObjectProvider<ExceptionMessageCustomizer<?>> exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
    }

    @ExceptionHandler(Throwable.class)
    @SuppressWarnings("all")
    public RestResp<Object> handleException(HttpServletRequest request, Throwable exception){
        dealStackTraceElement(exception);
        Object data = null;
        int errorCode = BaseException.DEFAULT_CODE;
        if(exception instanceof MethodArgumentTypeMismatchException){
            errorCode = PARAM_PARSE_ERROR;
        }else if(exception instanceof MethodArgumentNotValidException){
            errorCode = PARAM_PARSE_ERROR;
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) exception;
            data = dealBindingResult(ex.getBindingResult());
        }else if(exception instanceof BindException){
            errorCode = PARAM_PARSE_ERROR;
            BindException ex = (BindException) exception;
            data = dealBindingResult(ex.getBindingResult());
        }

        String errorMsg = exception.getMessage();

        if(StringUtils.isEmpty(errorMsg)){//无异常信息,走服务端内部错误
            errorMsg = getErrorMsg(SERVICE_ERROR);
        }

        RestResp<Object> restResp = new RestResp<>(errorCode,errorMsg);

        if(data != null && setValidatorResult){//参数校验具体错误数据信息
            restResp.setData(data);
        }

        for (ExceptionMessageCustomizer exceptionHandler : exceptionHandlers) {
            Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(exceptionHandler.getClass(),ExceptionMessageCustomizer.class);
            if(requiredType != null && requiredType.isInstance(exception)){
                restResp.setErrorInfo(exceptionHandler.handle(exception));//设置错误信息提示
                break;
            }
        }

        logError(restResp.getErrorInfo(),exception);

        return restResp;
    }

    private Object dealBindingResult(BindingResult bindingResult){
        return bindingResult.getAllErrors().stream().map(e -> {
            if(e instanceof FieldError){
                FieldError fieldError = (FieldError) e;
                return new ValidationErrorBean(e.getDefaultMessage(),fieldError.getField(), fieldError.getRejectedValue()==null?null:fieldError.getRejectedValue().toString());
            }
            return new ValidationErrorBean(e.getDefaultMessage(),e.getObjectName(), null);
           }
        ).collect(Collectors.toList());

    }

    @ExceptionHandler(ServletException.class)
    public RestResp<Object> handleServletException(HttpServletRequest request, ServletException exception){
        dealStackTraceElement(exception);
        int code = HTTP_ERROR_500;
        if (exception instanceof NoHandlerFoundException) {
            code = HTTP_ERROR_404;
        } else if (exception instanceof HttpRequestMethodNotSupportedException) {
            code = HTTP_ERROR_405;
        } else if (exception instanceof HttpMediaTypeException) {
            code = HTTP_ERROR_406;
        } else if (exception instanceof UnavailableException) {
            code = HTTP_ERROR_503;
        }
        String errMsg = getErrorMsg(code);
        logError(errMsg,exception);
        return buildErrorMessage(code);
    }

    @ExceptionHandler(BaseException.class)
    public RestResp<Object> handleBaseException(HttpServletRequest request, BaseException exception){
        dealStackTraceElement(exception);
        Integer errorCode = exception.getCode();
        String errMsg = exception.getMsg();
        if(exception instanceof JsonException){
            errorCode = ExceptionKeys.JSON_PARSE_ERROR;
            errMsg = getErrorMsg(errorCode);
        }
        logError(errMsg,exception);
        return buildErrorMessage(errorCode==null?BaseException.DEFAULT_CODE:errorCode,errMsg==null?exception.getMessage():errMsg);
    }

}
