package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionHelper;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionPostProcessor;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.GenericExceptionResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.error.GlobalExceptionViewResolver;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.util.ConcurrentReferenceHashMap;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * global exception handler
 *
 * @author DingHao
 * @since 2021/5/8 17:27
 */
@RestControllerAdvice
public class GlobalExceptionHandler implements EnvironmentAware, ApplicationContextAware, Ordered {
    private static final Map<Class<?>, ExceptionResolver<Throwable>> exceptionTypeCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ResolvableType> exceptionResolverTypeCache = new ConcurrentReferenceHashMap<>();

    private final GlobalExceptionProperties properties;

    private ExceptionHelper exceptionHelper;

    private final GlobalExceptionViewResolver viewResolver;
    private final ExceptionPostProcessor<?> exceptionPostProcessor;
    private String[] exceptionResolverNames;
    private ApplicationContext applicationContext;

    public GlobalExceptionHandler(GlobalExceptionProperties properties,
                                  ObjectProvider<ExceptionPostProcessor<?>> exceptionPostProcessors,
                                  ObjectProvider<GlobalExceptionViewResolver> globalExceptionViewResolvers) {
        this.properties = properties;
        this.exceptionPostProcessor = exceptionPostProcessors.getIfUnique();
        this.viewResolver = globalExceptionViewResolvers.getIfUnique();
    }

    @ExceptionHandler(Throwable.class)
    public Object handleException(HttpServletRequest request, Throwable exception) throws Throwable{
        if(Objects.nonNull(viewResolver) && viewResolver.support(request)){
            exceptionHelper.logError(exception);
            return viewResolver.view(request, exception);
        }
        RestResp<Throwable> resp = null;
        for (String s : exceptionResolverNames) {
            Class<? extends Throwable> exClass = exception.getClass();
            ExceptionResolver<Throwable> exceptionResolver = exceptionTypeCache.computeIfAbsent(exClass, a -> supportsExceptionType(s, exClass));
            if (exceptionResolver == null) {
                continue;
            }
            resp = exceptionResolver.resolveException(request, exception);
            if (resp == null) {
                continue;
            }
            break;
        }
        if(Objects.isNull(resp)){
            RestResp<Object> rs = exceptionHelper.doHandleException(ex -> {
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
            resp = RestResp.error(rs.getErrorCode(),rs.getErrorInfo());
        }
        if(properties.isOverrideExMsg()){
            String message = properties.getErrorCodeMsg().get(resp.getErrorCode());
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

    @SuppressWarnings("unchecked")
    private ExceptionResolver<Throwable> supportsExceptionType(String beanName, Class<? extends Throwable> exceptionType) {
        DefaultListableBeanFactory beanFactory = null;
        if (applicationContext instanceof DefaultListableBeanFactory) {
            beanFactory = (DefaultListableBeanFactory) applicationContext;
        } else if (applicationContext instanceof GenericApplicationContext) {
            beanFactory = ((GenericApplicationContext) applicationContext).getDefaultListableBeanFactory();
        }
        if (beanFactory != null) {
            ExceptionResolver<Throwable> exceptionResolver = applicationContext.getBean(beanName, ExceptionResolver.class);
            if(exceptionResolver instanceof GenericExceptionResolver){
                GenericExceptionResolver genericExceptionResolver = (GenericExceptionResolver) exceptionResolver;
                if(genericExceptionResolver.supportsType(exceptionType)){
                    return exceptionResolver;
                }
            }
            ResolvableType declaredExceptionType = resolveDeclaredExceptionType(exceptionResolver.getClass());
            if (declaredExceptionType == null || declaredExceptionType.isAssignableFrom(Throwable.class)) {
                Class<?> targetClass = AopUtils.getTargetClass(exceptionResolver);
                if (targetClass != exceptionResolver.getClass()) {
                    declaredExceptionType = resolveDeclaredExceptionType(targetClass);
                }
            }
            if (declaredExceptionType == null || declaredExceptionType.isAssignableFrom(exceptionType)) {
                try {
                    BeanDefinition bd = beanFactory.getMergedBeanDefinition(beanName);
                    ResolvableType genericExceptionType = bd.getResolvableType().as(ExceptionResolver.class).getGeneric();
                    if (genericExceptionType == ResolvableType.NONE || genericExceptionType.isAssignableFrom(exceptionType)) {
                        return exceptionResolver;
                    }
                } catch (NoSuchBeanDefinitionException ex) {
                    //
                }
            }
        }
        return null;
    }

    private static ResolvableType resolveDeclaredExceptionType(Class<?> exceptionResolverType) {
        ResolvableType exceptionType = exceptionResolverTypeCache.computeIfAbsent(exceptionResolverType, e -> ResolvableType.forClass(exceptionResolverType).as(ExceptionResolver.class).getGeneric());
        return (exceptionType != ResolvableType.NONE ? exceptionType : null);
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
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.exceptionResolverNames = applicationContext.getBeanNamesForType(ExceptionResolver.class);

    }
    @Override
    public int getOrder() {
        return properties.getOrder();
    }

}
