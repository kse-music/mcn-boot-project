package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionMessageCustomizer;
import cn.hiboot.mcn.autoconfigure.web.exception.error.GlobalExceptionViewResolver;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.ValidationErrorBean;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * global exception handler
 *
 * @author DingHao
 * @since 2021/5/8 17:27
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractExceptionHandler {

    private final ObjectProvider<ExceptionMessageCustomizer> exceptionHandlers;

    public GlobalExceptionHandler(ObjectProvider<ExceptionMessageCustomizer> exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
    }

    @Autowired(required = false)
    public void setErrorView(GlobalExceptionViewResolver exceptionViewResolver) {
        super.setErrorViewResolver(exceptionViewResolver);
    }

    @Override
    public RestResp<Object> buildErrorData(HttpServletRequest request, Throwable exception) throws Throwable {
        int errorCode = BaseException.DEFAULT_CODE;
        Object data = null;
        if(exception instanceof BaseException){
            errorCode = ((BaseException) exception).getCode();
        }else if(exception instanceof MethodArgumentTypeMismatchException){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
        }else if(exception instanceof ServletRequestBindingException){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
        }else if(exception instanceof BindException){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            BindException ex = (BindException) exception;
            data = dealBindingResult(ex.getBindingResult());
        }else if(exception instanceof ServletException){
            errorCode =  mappingCode(((ServletException) exception));
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

    private int mappingCode(ServletException exception) throws ServletException {
        if(isOverrideHttpError()){
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

}
