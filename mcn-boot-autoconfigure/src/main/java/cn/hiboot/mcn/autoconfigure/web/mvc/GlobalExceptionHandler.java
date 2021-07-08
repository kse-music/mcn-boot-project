package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.web.exception.handler.AbstractExceptionHandler;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.UnCaughtExceptionHandler;
import cn.hiboot.mcn.core.model.ValidationErrorBean;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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

    @ExceptionHandler(Exception.class)
    @SuppressWarnings("all")
    public RestResp<Object> handleException(HttpServletRequest request, Exception exception){
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

        RestResp<Object> restResp = new RestResp<>(errorCode,exception.getMessage());

        if(data != null){//参数校验具体错误数据信息
            restResp.setData(data);
        }

        for (UnCaughtExceptionHandler exceptionHandler : exceptionHandlers) {
            Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(exceptionHandler.getClass(),UnCaughtExceptionHandler.class);
            if(requiredType != null && requiredType.isInstance(exception)){
                exceptionHandler.handle(restResp, exception);//设置错误信息或具体错误数据
                restResp.setErrorCode(errorCode);//防止code码被修改
                break;
            }
        }

        if(StringUtils.isEmpty(restResp.getErrorInfo())){//无异常信息,走服务端内部错误
            restResp = buildErrorMessage(SERVICE_ERROR);
        }

        logger.error("ErrorMsg = {}",restResp.getErrorInfo(),exception);

        return restResp;
    }

    @Autowired
    private ObjectProvider<UnCaughtExceptionHandler<?>> exceptionHandlers;

    private Object dealBindingResult(BindingResult bindingResult){
        return bindingResult.getAllErrors().stream().map((e) -> {
            if(e instanceof FieldError){
                return new ValidationErrorBean(e.getDefaultMessage(),((FieldError )e).getField(), ((FieldError )e).getRejectedValue()==null?null:((FieldError )e).getRejectedValue().toString());
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
        logger.error("ErrorMsg = {}",errMsg,exception);
        return buildErrorMessage(code);
    }

    @ExceptionHandler(BaseException.class)
    public RestResp<Object> handleBaseException(HttpServletRequest request, BaseException exception){
        dealStackTraceElement(exception);
        Integer errorCode = exception.getCode();
        String errMsg = exception.getMsg();
        dealStackTraceElement(exception);
        logger.error("ErrorMsg = {}",errMsg,exception);
        return buildErrorMessage(errorCode,errMsg);
    }

}
