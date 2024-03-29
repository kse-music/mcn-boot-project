package cn.hiboot.mcn.cloud.feign;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
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
        if (!(cause instanceof FeignException exception)) {
            return remoteError(errorMessage);
        }
        if(exception.contentUTF8().isEmpty()){
            return remoteError(errorMessage);
        }
        return remoteError(exception.contentUTF8());
    }

    private RestResp<Object> remoteError(String msg){
        return RestResp.error(ExceptionKeys.REMOTE_SERVICE_ERROR, msg);
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
