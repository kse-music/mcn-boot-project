package cn.hiboot.mcn.autoconfigure.web.exception;

import org.springframework.core.ResolvableType;

/**
 * GenericExceptionResolver
 *
 * @author DingHao
 * @since 2023/2/6 10:10
 */
public interface GenericExceptionResolver extends ExceptionResolver<Throwable> {

    default boolean supportsType(Class<Throwable> type) {
        return supportsType(ResolvableType.forClass(type));
    }

    boolean supportsType(ResolvableType type);

}

