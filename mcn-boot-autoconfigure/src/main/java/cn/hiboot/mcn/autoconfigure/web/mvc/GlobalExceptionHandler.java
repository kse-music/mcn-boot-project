package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.web.exception.HttpStatusCodeResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.error.GlobalExceptionViewResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ExceptionHandler;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Objects;

/**
 * global exception handler
 *
 * @author DingHao
 * @since 2021/5/8 17:27
 */
@RestControllerAdvice
public class GlobalExceptionHandler implements HttpStatusCodeResolver,Ordered {

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
        if(ex instanceof ServletRequestBindingException){
            return ExceptionKeys.PARAM_PARSE_ERROR;
        }else if(ex instanceof ServletException){
            if (ex.getCause() instanceof Error) {
                exceptionHandler.handleError((Error) ex.getCause());
                return ExceptionKeys.SERVICE_ERROR;
            }
            int code = ExceptionKeys.HTTP_ERROR_500;
            if (ex instanceof NoHandlerFoundException) {
                code = ExceptionKeys.HTTP_ERROR_404;
            } else if (ex instanceof HttpRequestMethodNotSupportedException) {
                code = ExceptionKeys.HTTP_ERROR_405;
            } else if (ex instanceof HttpMediaTypeException) {
                code = ExceptionKeys.HTTP_ERROR_406;
            } else if (ex instanceof UnavailableException) {
                code = ExceptionKeys.HTTP_ERROR_503;
            }
            return code;
        }
        return null;
    }


    @Override
    public int getOrder() {
        return exceptionHandler.config().getOrder();
    }

}
