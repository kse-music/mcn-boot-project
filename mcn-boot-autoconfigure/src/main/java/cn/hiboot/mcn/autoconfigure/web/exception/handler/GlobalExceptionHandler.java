package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionHelper;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionMessageProcessor;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionPostProcessor;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.error.GlobalExceptionViewResolver;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
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

    private final GlobalExceptionProperties properties;

    private ExceptionHelper exceptionHelper;

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
            exceptionHelper.logError(exception);
            return viewResolver.view(request, exception);
        }
        if(Objects.nonNull(exceptionPostProcessor)){
            Object o = exceptionPostProcessor.beforeHandle(request, exception);
            if(Objects.nonNull(o)){
                exceptionHelper.logError(exception);
                return o;
            }
        }
        RestResp<Object> resp = null;
        for (ExceptionResolver resolver : exceptionResolvers) {
            if(resolver.support(request, exception)){
                resp = resolver.resolveException(request, exception);
                break;
            }
        }
        if(Objects.isNull(resp)){
            resp = exceptionHelper.doHandleException(ex -> {
                if(ex instanceof ServletRequestBindingException){
                    return ExceptionKeys.PARAM_PARSE_ERROR;
                }else if(ex instanceof ServletException){
                    if (ex instanceof NestedServletException && ex.getCause() instanceof Error) {
                        exceptionHelper.handleError((Error) ex.getCause());
                    }
                    return mappingCode((ServletException) exception);
                }
                return null;
            }, exception);
        }
        if(properties.isOverrideExMsg() && exceptionMessageProcessor != null){
            String message = exceptionMessageProcessor.process(resp.getErrorCode());
            if(Objects.nonNull(message)){
                resp.setErrorInfo(message);
            }
        }
        exceptionHelper.logError(exception);
        if(Objects.nonNull(exceptionPostProcessor)){
            Object o = exceptionPostProcessor.afterHandle(request, exception, resp);
            if(Objects.nonNull(o)){
                return o;
            }
        }
        return resp;
    }

    private int mappingCode(ServletException exception) throws ServletException {
        if(exceptionHelper.isOverrideHttpError()){
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
        this.exceptionHelper = new ExceptionHelper(properties,environment);
    }

    @Override
    public int getOrder() {
        return properties.getOrder();
    }

}
