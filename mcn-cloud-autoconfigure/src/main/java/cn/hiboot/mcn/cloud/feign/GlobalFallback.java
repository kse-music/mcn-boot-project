package cn.hiboot.mcn.cloud.feign;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionHelper;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * FeignFallback
 *
 * @author DingHao
 * @since 2021/7/4 10:18
 */
public class GlobalFallback<T> implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GlobalFallback.class);

    private final Class<T> targetType;
    private final String targetName;
    private final Throwable cause;

    public GlobalFallback(Class<T> targetType, String targetName, Throwable cause) {
        this.targetType = targetType;
        this.targetName = targetName;
        this.cause = cause;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        String errorMessage = cause.getMessage();
        log.error("GlobalFallback:[{}.{}] serviceId:[{}] message:[{}]", targetType.getName(), method.getName(), targetName, errorMessage);
        if (!(cause instanceof FeignException)) {
            return ExceptionHelper.error(errorMessage);
        }
        FeignException exception = (FeignException) cause;
        if(exception.contentUTF8().isEmpty()){
            return ExceptionHelper.error(errorMessage);
        }
        return ExceptionHelper.error(exception.contentUTF8());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GlobalFallback<?> that = (GlobalFallback<?>) o;
        return targetType.equals(that.targetType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetType);
    }

}
