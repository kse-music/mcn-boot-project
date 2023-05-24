package cn.hiboot.mcn.autoconfigure.web.exception;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.GlobalExceptionProperties;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ValidationErrorBean;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ValidationExceptionHandler;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AbstractExceptionHandler
 *
 * @author DingHao
 * @since 2023/5/24 13:25
 */
public abstract class AbstractExceptionHandler implements Ordered {
    private final Logger log = LoggerFactory.getLogger(AbstractExceptionHandler.class);

    /**
     * 非BaseException的默认code码
     */
    private static final int DEFAULT_ERROR_CODE = 999998;

    private static final Map<Class<?>, List<ExceptionResolver<Throwable>>> exceptionResolverCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ResolvableType> exceptionResolverTypeCache = new ConcurrentReferenceHashMap<>();

    private final boolean validationExceptionPresent;
    private final String basePackage;
    private final boolean overrideHttpError;
    private final GlobalExceptionProperties properties;
    private final String[] exceptionResolverNames;
    private final ApplicationContext applicationContext;

    public AbstractExceptionHandler(GlobalExceptionProperties properties) {
        this.properties = properties;
        this.validationExceptionPresent = ClassUtils.isPresent("javax.validation.ValidationException", getClass().getClassLoader());
        this.applicationContext = SpringBeanUtils.getApplicationContext();
        this.exceptionResolverNames = applicationContext.getBeanNamesForType(ExceptionResolver.class);
        this.basePackage = applicationContext.getEnvironment().getProperty(ConfigProperties.APP_BASE_PACKAGE);
        this.overrideHttpError = applicationContext.getEnvironment().getProperty("http.error.override",Boolean.class,true);
    }

    public RestResp<Throwable> handleException(Throwable exception) {
        RestResp<Throwable> resp = null;
        Class<? extends Throwable> exClass = exception.getClass();
        List<ExceptionResolver<Throwable>> exceptionResolvers = exceptionResolverCache.get(exClass);
        if (exceptionResolvers == null) {
            exceptionResolvers = Arrays.stream(exceptionResolverNames).map(s -> supportsExceptionType(s, exClass)).filter(Objects::nonNull).collect(Collectors.toList());
            exceptionResolverCache.put(exClass,exceptionResolvers);
        }
        for (ExceptionResolver<Throwable> exceptionResolver : exceptionResolvers) {
            resp = exceptionResolver.resolve(exception);
            if (resp != null) {
                break;
            }
        }
        if(Objects.isNull(resp)){
            RestResp<Object> rs = doHandleException(customHandleException(), exception);
            resp = RestResp.error(rs.getErrorCode(),rs.getErrorInfo());
        }
        if(properties.isOverrideExMsg()){
            String message = properties.getErrorCodeMsg().get(resp.getErrorCode());
            if(Objects.nonNull(message)){
                resp.setErrorInfo(message);
            }
        }
        logError(exception);
        return resp;
    }

    private RestResp<Object> doHandleException(Function<Throwable,Integer> customHandler, Throwable exception) {
        Integer errorCode = DEFAULT_ERROR_CODE;
        List<ValidationErrorBean> data = null;
        if(exception instanceof BaseException){
            errorCode = ((BaseException) exception).getCode();
        }else if(exception instanceof MethodArgumentTypeMismatchException){
            errorCode = ExceptionKeys.PARAM_TYPE_ERROR;
        }else if(exception instanceof MaxUploadSizeExceededException){
            errorCode = ExceptionKeys.UPLOAD_FILE_SIZE_ERROR;
        }else if(exception instanceof BindException){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            BindException ex = (BindException) exception;
            data = dealBindingResult(ex.getBindingResult());
        }else if(validationExceptionPresent && ValidationExceptionHandler.support(exception)){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            data = ValidationExceptionHandler.handle(exception);
        }else if(exception instanceof HttpMessageNotReadableException || exception instanceof InvalidFormatException) {
            errorCode = ExceptionKeys.JSON_PARSE_ERROR;
        }else if(exception instanceof VirtualMachineError){
            errorCode = ExceptionKeys.HTTP_ERROR_500;
            handleError((Error) exception.getCause());
        }else if(customHandler != null){
            Integer error = customHandler.apply(exception);
            if(error != null){
                errorCode = error;
            }
        }
        return buildErrorMessage(errorCode,data,exception);
    }

    private RestResp<Object> buildErrorMessage(Integer code,List<ValidationErrorBean> data,Throwable t){
        String msg = properties.isReturnOriginExMsg() ? getMessage(t) : ErrorMsg.getErrorMsg(getErrorCode(code));
        RestResp<Object> resp = RestResp.error(code, msg);
        if(McnUtils.isNotNullAndEmpty(data)){
            if(properties.isValidateResultToErrorInfo()){
                ValidationErrorBean validationErrorBean = data.get(0);
                String message = validationErrorBean.getMessage();
                resp.setErrorInfo(properties.isAppendField() ? validationErrorBean.getPath().concat(message) : message);
            }
            if(properties.isReturnValidateResult()){//设置参数校验具体错误数据信息
                resp.setData(data);
            }
        }
        return resp;
    }

    protected String getMessage(Throwable t) {
        return t.getMessage();
    }

    private Integer getErrorCode(Integer errorCode){
        return errorCode == DEFAULT_ERROR_CODE ? ExceptionKeys.SERVICE_ERROR : errorCode;
    }

    protected Function<Throwable,Integer> customHandleException() {
        return null;
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

    protected void handleError(Error error) {
        if(error instanceof VirtualMachineError){
            GlobalExceptionProperties.JvmError jvm = properties.getJvmError();
            if(jvm != null && jvm.isExit()){
                System.exit(jvm.getStatus());
            }
        }
    }

    protected boolean isOverrideHttpError() {
        return overrideHttpError;
    }

    public void logError(Throwable t){
        if(properties.isRemoveFrameworkStack()){
            dealCurrentStackTraceElement(t);
            Throwable[] suppressed = t.getSuppressed();
            for (Throwable throwable : suppressed) {
                dealCurrentStackTraceElement(throwable);
            }
        }
        log.error("The exception information is as follows",t);
    }


    private void dealCurrentStackTraceElement(Throwable exception){
        if(Objects.isNull(exception.getCause())){//is self
            return;
        }
        exception.setStackTrace(Arrays.stream(exception.getStackTrace()).filter(s -> s.getClassName().contains(basePackage)).toArray(StackTraceElement[]::new));
    }


    @Override
    public int getOrder() {
        return properties.getOrder();
    }

}
