package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.web.exception.HttpStatusCodeResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.error.GlobalExceptionViewResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ExceptionHandler;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.model.result.RestResp;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * global exception handler
 *
 * @author DingHao
 * @since 2021/5/8 17:27
 */
@RestControllerAdvice
public class GlobalExceptionHandler implements HttpStatusCodeResolver, Ordered {

    @Value("${http.error.override:true}")
    private boolean overrideHttpError;
    private final GlobalExceptionViewResolver viewResolver;
    private final ExceptionHandler exceptionHandler;

    public GlobalExceptionHandler(ExceptionHandler exceptionHandler, ObjectProvider<GlobalExceptionViewResolver> globalExceptionViewResolvers) {
        this.exceptionHandler = exceptionHandler;
        this.viewResolver = globalExceptionViewResolvers.getIfUnique();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Throwable.class)
    public Object handleException(HttpServletRequest request, Throwable exception) {
        if(Objects.nonNull(viewResolver) && viewResolver.support(request)){
            exceptionHandler.logError(exception);
            return viewResolver.view(request, exception);
        }
        RestResp<Throwable> resp = exceptionHandler.handleException(exception);
        if (overrideHttpError) {
            return resp;
        }
        request.setAttribute(ExceptionHandler.EXCEPTION_HANDLE_RESULT_ATTRIBUTE, resp);
        throw ServiceException.newInstance(exception);
    }

    @Override
    public Integer resolve(Throwable ex) {
        if(ex instanceof ServletException){
            if (ex.getCause() instanceof Error error) {
                exceptionHandler.handleError(error);
                return ExceptionKeys.SERVICE_ERROR;
            }
            if (ex instanceof ErrorResponse e) {
                return mappingCode(e.getStatusCode());
            }else if (ex instanceof UnavailableException) {
                return ExceptionKeys.HTTP_ERROR_503;
            }
            return ExceptionKeys.HTTP_ERROR_500;
        }
        return null;
    }


    @Override
    public int getOrder() {
        return exceptionHandler.config().getOrder();
    }

}
