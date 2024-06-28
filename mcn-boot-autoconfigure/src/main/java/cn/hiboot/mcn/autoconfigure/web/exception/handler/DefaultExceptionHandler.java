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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DefaultExceptionHandler
 *
 * @author DingHao
 * @since 2023/5/24 13:25
 */
public class DefaultExceptionHandler implements ExceptionHandler, ApplicationContextAware, SmartInitializingSingleton {
    private final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    /**
     * 非BaseException的默认code码
     */
    private static final int DEFAULT_ERROR_CODE = 999998;

    private static final Map<Class<?>, List<ExceptionResolver<Throwable>>> exceptionResolverCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ResolvableType> exceptionResolverTypeCache = new ConcurrentReferenceHashMap<>();

    private boolean validationExceptionPresent;
    private String basePackage;
    private final ExceptionProperties properties;
    private ApplicationContext applicationContext;
    private final List<ExceptionResolver<Throwable>> exceptionResolvers = new ArrayList<>(8);
    private List<HttpStatusCodeResolver> httpStatusCodeResolvers;

    protected DefaultExceptionHandler(ExceptionProperties properties) {
        this.properties = properties;
    }

    @Override
    public ExceptionProperties config() {
        return properties;
    }

    @Override
    public RestResp<Throwable> handleException(Throwable exception) {
        RestResp<Throwable> resp = null;
        List<ExceptionResolver<Throwable>> exceptionResolvers = exceptionResolverCache.computeIfAbsent(exception.getClass(), exClass -> this.exceptionResolvers.stream().filter(s -> supportsExceptionType(s, ResolvableType.forClass(exClass))).collect(Collectors.toList()));
        for (ExceptionResolver<Throwable> exceptionResolver : exceptionResolvers) {
            resp = exceptionResolver.resolve(exception);
            if (resp != null) {
                break;
            }
        }
        if (Objects.isNull(resp)) {
            resp = doHandleException(exception);
        }
        if (properties.isOverrideExMsg()) {
            String message = properties.getErrorCodeMsg().get(resp.getErrorCode());
            if (Objects.nonNull(message)) {
                resp.setErrorInfo(message);
            }
        }
        logError(exception);
        return resp;
    }

    private RestResp<Throwable> doHandleException(Throwable exception) {
        Integer errorCode = DEFAULT_ERROR_CODE;
        List<ValidationErrorBean> data = null;
        if (exception instanceof BaseException ex) {
            errorCode = ex.getCode();
        } else if (exception instanceof MethodArgumentTypeMismatchException) {
            errorCode = ExceptionKeys.PARAM_TYPE_ERROR;
        } else if (exception instanceof MaxUploadSizeExceededException) {
            errorCode = ExceptionKeys.UPLOAD_FILE_SIZE_ERROR;
        } else if (exception instanceof BindException ex) {
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            data = dealBindingResult(ex.getBindingResult());
        } else if (validationExceptionPresent && ValidationExceptionHandler.support(exception)) {
            errorCode = ExceptionKeys.PARAM_PARSE_ERROR;
            data = ValidationExceptionHandler.handle(exception);
        } else if (exception instanceof HttpMessageNotReadableException || exception instanceof InvalidFormatException) {
            errorCode = ExceptionKeys.JSON_PARSE_ERROR;
        } else if (exception instanceof VirtualMachineError) {
            errorCode = ExceptionKeys.HTTP_ERROR_500;
            handleError((Error) exception.getCause());
        }
        Integer error = httpStatusCodeResolvers.stream().map(resolver -> resolver.resolve(exception)).filter(Objects::nonNull).findFirst().orElse(null);
        if (error != null) {
            errorCode = error;
        }
        return result(errorCode, exception, data);
    }

    private RestResp<Throwable> result(Integer errorCode, Throwable exception, List<ValidationErrorBean> data) {
        String msg = properties.isReturnOriginExMsg() ? getMessage(exception) : ErrorMsg.getErrorMsg(getErrorCode(errorCode));
        RestResp<Throwable> resp = RestResp.error(errorCode, msg);
        if (McnUtils.isNotNullAndEmpty(data)) {
            if (properties.isValidateResultToErrorInfo()) {
                ValidationErrorBean validationErrorBean = data.get(0);
                String message = validationErrorBean.getMessage();
                resp.setErrorInfo(properties.isAppendField() ? validationErrorBean.getPath().concat(message) : message);
            }
            if (properties.isReturnValidateResult()) {//设置参数校验具体错误数据信息
                resp.setData(new ThrowableData(data));
            }
        }
        return resp;
    }

    private String getMessage(Throwable t) {
        if (t == null) {
            return null;
        }
        String msg = null;
        if (t instanceof ResponseStatusException responseStatusException) {
            msg = responseStatusException.getReason();
        }
        return msg == null ? t.getMessage() : msg;
    }

    private Integer getErrorCode(Integer errorCode) {
        return errorCode == DEFAULT_ERROR_CODE ? ExceptionKeys.SERVICE_ERROR : errorCode;
    }

    private boolean supportsExceptionType(ExceptionResolver<Throwable> exceptionResolver, ResolvableType exceptionType) {
        if (exceptionResolver instanceof GenericExceptionResolver genericExceptionResolver) {
            return genericExceptionResolver.supportsType(exceptionType);
        }
        if (ignoreExceptionType(exceptionResolver.getClass(), exceptionType)) {
            Class<?> targetClass = AopUtils.getTargetClass(exceptionResolver);
            if (targetClass != exceptionResolver.getClass()) {
                if (ignoreExceptionType(targetClass, exceptionType)) {
                    return false;
                }
            }
            return false;
        }
        DefaultListableBeanFactory beanFactory =
                applicationContext instanceof DefaultListableBeanFactory bf ? bf :
                        applicationContext instanceof GenericApplicationContext context ? context.getDefaultListableBeanFactory() : null;
        if (beanFactory == null) {
            return true;
        }
        try {
            String beanName = getBeanName(exceptionResolver);
            if (beanName == null) {
                return true;
            }
            BeanDefinition bd = beanFactory.getMergedBeanDefinition(beanName);
            ResolvableType genericExceptionType = bd.getResolvableType().as(ExceptionResolver.class).getGeneric();
            return genericExceptionType == ResolvableType.NONE || genericExceptionType.isAssignableFrom(exceptionType);
        } catch (NoSuchBeanDefinitionException ex) {
            // Ignore - no need to check resolvable type for manually registered singleton
            return true;
        }
    }

    private String getBeanName(Object singletonBean) {
        String[] beanNames = applicationContext.getBeanNamesForType(singletonBean.getClass());
        if (beanNames.length > 0) {
            return beanNames[0];
        }
        return null;
    }

    private boolean ignoreExceptionType(Class<?> exceptionResolverType, ResolvableType exceptionType) {
        ResolvableType declaredEventType = resolveDeclaredExceptionType(exceptionResolverType);
        return !(declaredEventType == null || declaredEventType.isAssignableFrom(exceptionType));
    }

    private ResolvableType resolveDeclaredExceptionType(Class<?> exceptionResolverType) {
        ResolvableType exceptionType = exceptionResolverTypeCache.computeIfAbsent(exceptionResolverType, e -> ResolvableType.forClass(exceptionResolverType).as(ExceptionResolver.class).getGeneric());
        return exceptionType != ResolvableType.NONE ? exceptionType : null;
    }

    private List<ValidationErrorBean> dealBindingResult(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream().map(e -> {
                    if (e instanceof FieldError fieldError) {
                        return new ValidationErrorBean(e.getDefaultMessage(), fieldError.getField(), fieldError.getRejectedValue() == null ? null : fieldError.getRejectedValue().toString());
                    }
                    return new ValidationErrorBean(e.getDefaultMessage(), e.getObjectName(), null);
                }
        ).collect(Collectors.toList());
    }

    @Override
    public void handleError(Error error) {
        if (error instanceof VirtualMachineError) {
            ExceptionProperties.JvmError jvm = properties.getJvmError();
            if (jvm != null && jvm.isExit()) {
                System.exit(jvm.getStatus());
            }
        }
    }

    @Override
    public void logError(Throwable t) {
        if (properties.isLogExMsg()) {
            if (properties.isRemoveFrameworkStack()) {
                dealCurrentStackTraceElement(t);
                Throwable[] suppressed = t.getSuppressed();
                for (Throwable throwable : suppressed) {
                    dealCurrentStackTraceElement(throwable);
                }
            }
            log.error("The exception information is as follows", t);
        }
    }

    private void dealCurrentStackTraceElement(Throwable exception) {
        if (Objects.isNull(exception.getCause())) {//is self
            return;
        }
        exception.setStackTrace(Arrays.stream(exception.getStackTrace()).filter(s -> s.getClassName().contains(basePackage)).toArray(StackTraceElement[]::new));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.validationExceptionPresent = ClassUtils.isPresent("jakarta.validation.ValidationException", getClass().getClassLoader());
        this.basePackage = applicationContext.getEnvironment().getProperty(ConfigProperties.APP_BASE_PACKAGE);
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.httpStatusCodeResolvers = applicationContext.getBeanProvider(HttpStatusCodeResolver.class).orderedStream().collect(Collectors.toList());
        ObjectProvider<ExceptionResolver<Throwable>> beanProvider = applicationContext.getBeanProvider(ResolvableType.forClass(ExceptionResolver.class));
        beanProvider.orderedStream().forEach(exceptionResolvers::add);
    }

    @JsonIgnoreProperties({"cause", "stackTrace", "message", "suppressed", "localizedMessage"})
    static class ThrowableData extends Throwable {
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
