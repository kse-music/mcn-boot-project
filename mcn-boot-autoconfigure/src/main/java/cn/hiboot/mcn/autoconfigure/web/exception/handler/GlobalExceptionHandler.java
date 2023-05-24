package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.web.exception.AbstractExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.exception.error.GlobalExceptionViewResolver;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.NestedServletException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.function.Function;

/**
 * global exception handler
 *
 * @author DingHao
 * @since 2021/5/8 17:27
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractExceptionHandler {

    private final GlobalExceptionViewResolver viewResolver;

    public GlobalExceptionHandler(GlobalExceptionProperties properties, ObjectProvider<GlobalExceptionViewResolver> globalExceptionViewResolvers) {
        super(properties);
        this.viewResolver = globalExceptionViewResolvers.getIfUnique();
    }

    @ExceptionHandler(Throwable.class)
    public Object handleException(HttpServletRequest request, Throwable exception){
        if(Objects.nonNull(viewResolver) && viewResolver.support(request)){
            logError(exception);
            return viewResolver.view(request, exception);
        }
        return handleException(exception);
    }

    @Override
    protected Function<Throwable, Integer> customHandleException() {
        return ex -> {
            if(ex instanceof ServletRequestBindingException){
                return ExceptionKeys.PARAM_PARSE_ERROR;
            }else if(ex instanceof ServletException){
                if (ex instanceof NestedServletException && ex.getCause() instanceof Error) {
                    handleError((Error) ex.getCause());
                }
                return mappingCode((ServletException) ex);
            }
            return null;
        };
    }

    private int mappingCode(ServletException exception){
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
        return ExceptionKeys.SERVICE_ERROR;
    }

}
