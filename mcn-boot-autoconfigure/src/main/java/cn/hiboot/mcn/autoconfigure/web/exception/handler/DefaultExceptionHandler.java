package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionProperties;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.GenericExceptionResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.HttpStatusCodeResolver;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DefaultExceptionHandler
 *
 * @author DingHao
 * @since 2023/5/24 13:25
 */
public class DefaultExceptionHandler implements ExceptionHandler{
    private final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    /**
     * 非BaseException的默认code码
     */
    private static final int DEFAULT_ERROR_CODE = 999998;

    private static final Map<Class<?>, List<ExceptionResolver<Throwable>>> exceptionResolverCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ResolvableType> exceptionResolverTypeCache = new ConcurrentReferenceHashMap<>();

    private final boolean validationExceptionPresent;
    private final String basePackage;
    private final boolean overrideHttpError;
    private final ExceptionProperties properties;
    private final String[] exceptionResolverNames;
    private final ApplicationContext applicationContext;
    private final ObjectProvider<HttpStatusCodeResolver> httpStatusCodeResolvers;

    protected DefaultExceptionHandler(ExceptionProperties properties) {
        this.properties = properties;
        this.validationExceptionPresent = ClassUtils.isPresent("jakarta.validation.ValidationException", getClass().getClassLoader());
        this.applicationContext = SpringBeanUtils.getApplicationContext();
        this.httpStatusCodeResolvers = applicationContext.getBeanProvider(HttpStatusCodeResolver.class);
        this.exceptionResolverNames = applicationContext.getBeanNamesForType(ExceptionResolver.class);
        this.basePackage = applicationContext.getEnvironment().getProperty(ConfigProperties.APP_BASE_PACKAGE);
        this.overrideHttpError = applicationContext.getEnvironment().getProperty("http.error.override",Boolean.class,true);
    }

    @Override
    public ExceptionProperties config(){
        return properties;
    }

    @Override
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
            resp = doHandleException(exception);
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

    private RestResp<Throwable> doHandleException(Throwable exception) {
        Integer errorCode = DEFAULT_ERROR_CODE;
        List<ValidationErrorBean> data = null;
        if(exception instanceof BaseException ex){
            errorCode = ex.getCode();
        }else if(exception instanceof MethodArgumentTypeMismatchException){
            errorCode = ExceptionKeys.PARAM_TYPE_ERROR;
        }else if(exception instanceof MaxUploadSizeExceededException){
            errorCode = ExceptionKeys.UPLOAD_FILE_SIZE_ERROR;
        }else if(exception instanceof BindException ex){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            data = dealBindingResult(ex.getBindingResult());
        }else if(validationExceptionPresent && ValidationExceptionHandler.support(exception)){
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            data = ValidationExceptionHandler.handle(exception);
        }else if(exception instanceof HttpMessageNotReadableException || exception instanceof InvalidFormatException) {
            errorCode = ExceptionKeys.JSON_PARSE_ERROR;
        }else if(exception instanceof VirtualMachineError){
            errorCode = ExceptionKeys.HTTP_ERROR_500;
            handleError((Error) exception.getCause());
        }else if(overrideHttpError){
            Integer error = httpStatusCodeResolvers.orderedStream().map(resolver -> resolver.resolve(exception)).filter(Objects::nonNull).findFirst().orElse(null);
            if(error != null){
                errorCode = error;
            }
        }
        return result(errorCode, exception, data);
    }

    private RestResp<Throwable> result(Integer errorCode, Throwable exception, List<ValidationErrorBean> data) {
        String msg = properties.isReturnOriginExMsg() ? getMessage(exception) : ErrorMsg.getErrorMsg(getErrorCode(errorCode));
        RestResp<Throwable> resp = RestResp.error(errorCode, msg);
        if(McnUtils.isNotNullAndEmpty(data)){
            if(properties.isValidateResultToErrorInfo()){
                ValidationErrorBean validationErrorBean = data.get(0);
                String message = validationErrorBean.getMessage();
                resp.setErrorInfo(properties.isAppendField() ? validationErrorBean.getPath().concat(message) : message);
            }
            if(properties.isReturnValidateResult()){//设置参数校验具体错误数据信息
                resp.setData(new ThrowableData(data));
            }
        }
        return resp;
    }

    private String getMessage(Throwable t) {
        String msg = null;
        if(t instanceof ResponseStatusException){
            msg = ((ResponseStatusException) t).getReason();
        }
        return msg == null ? t.getMessage() : msg;
    }

    private Integer getErrorCode(Integer errorCode){
        return errorCode == DEFAULT_ERROR_CODE ? ExceptionKeys.SERVICE_ERROR : errorCode;
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
            if(exceptionResolver instanceof GenericExceptionResolver genericExceptionResolver){
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

    private ResolvableType resolveDeclaredExceptionType(Class<?> exceptionResolverType) {
        ResolvableType exceptionType = exceptionResolverTypeCache.computeIfAbsent(exceptionResolverType, e -> ResolvableType.forClass(exceptionResolverType).as(ExceptionResolver.class).getGeneric());
        return (exceptionType != ResolvableType.NONE ? exceptionType : null);
    }

    private List<ValidationErrorBean> dealBindingResult(BindingResult bindingResult){
        return bindingResult.getAllErrors().stream().map(e -> {
                    if(e instanceof FieldError fieldError){
                        return new ValidationErrorBean(e.getDefaultMessage(),fieldError.getField(), fieldError.getRejectedValue() == null ? null : fieldError.getRejectedValue().toString());
                    }
                    return new ValidationErrorBean(e.getDefaultMessage(),e.getObjectName(), null);
                }
        ).collect(Collectors.toList());
    }

    @Override
    public void handleError(Error error) {
        if(error instanceof VirtualMachineError){
            ExceptionProperties.JvmError jvm = properties.getJvmError();
            if(jvm != null && jvm.isExit()){
                System.exit(jvm.getStatus());
            }
        }
    }

    @Override
    public void logError(Throwable t){
        if(properties.isLogExMsg()){
            if(properties.isRemoveFrameworkStack()){
                dealCurrentStackTraceElement(t);
                Throwable[] suppressed = t.getSuppressed();
                for (Throwable throwable : suppressed) {
                    dealCurrentStackTraceElement(throwable);
                }
            }
            log.error("The exception information is as follows",t);
        }
    }


    private void dealCurrentStackTraceElement(Throwable exception){
        if(Objects.isNull(exception.getCause())){//is self
            return;
        }
        exception.setStackTrace(Arrays.stream(exception.getStackTrace()).filter(s -> s.getClassName().contains(basePackage)).toArray(StackTraceElement[]::new));
    }


    @JsonIgnoreProperties({"cause", "stackTrace", "message", "suppressed", "localizedMessage"})
    static class ThrowableData extends Throwable{
        private List<ValidationErrorBean> detail;
        ThrowableData(List<ValidationErrorBean> detail) {
            this.detail = detail;
        }

        public List<ValidationErrorBean> getDetail() {
            return detail;
        }

        public void setDetail(List<ValidationErrorBean> detail) {
            this.detail = detail;
        }
    }

}
