package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.web.exception.HttpStatusCodeResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.error.GlobalExceptionViewResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ExceptionHandler;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * global exception handler
 *
 * @author DingHao
 * @since 2021/5/8 17:27
 */
@RestControllerAdvice
public class GlobalExceptionHandler implements HttpStatusCodeResolver,Ordered {
    @Value("${http.error.override:true}")
    private boolean overrideHttpError;
    private final GlobalExceptionViewResolver viewResolver;
    private final ExceptionHandler exceptionHandler;

    public GlobalExceptionHandler(ExceptionHandler exceptionHandler, ObjectProvider<GlobalExceptionViewResolver> globalExceptionViewResolvers) {
        this.exceptionHandler = exceptionHandler;
        this.viewResolver = globalExceptionViewResolvers.getIfUnique();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Throwable.class)
    public Object handleException(HttpServletRequest request, Throwable exception){
        if(Objects.nonNull(viewResolver) && viewResolver.support(request)){
            exceptionHandler.logError(exception);
            return viewResolver.view(request, exception);
        }
        return exceptionHandler.handleException(exception);
    }

    @Override
    public Integer resolve(Throwable ex) {
        if(ex instanceof ServletException){
            if (ex.getCause() instanceof Error) {
                exceptionHandler.handleError((Error) ex.getCause());
                return ExceptionKeys.SERVICE_ERROR;
            }
            if(ex instanceof ServletRequestBindingException){
                return ExceptionKeys.PARAM_PARSE_ERROR;
            }
            if(overrideHttpError){
                if (ex instanceof NoHandlerFoundException) {
                    return ExceptionKeys.HTTP_ERROR_404;
                } else if (ex instanceof HttpRequestMethodNotSupportedException) {
                    return ExceptionKeys.HTTP_ERROR_405;
                } else if (ex instanceof HttpMediaTypeNotAcceptableException) {
                    return ExceptionKeys.HTTP_ERROR_406;
                }  else if (ex instanceof HttpMediaTypeNotSupportedException) {
                    return ExceptionKeys.HTTP_ERROR_415;
                }else if (ex instanceof UnavailableException) {
                    return ExceptionKeys.HTTP_ERROR_503;
                }
                return ExceptionKeys.HTTP_ERROR_500;
            }
            throw ServiceException.newInstance(ex);
        }
        return null;
    }


    @Override
    public int getOrder() {
        return exceptionHandler.config().getOrder();
    }

}
