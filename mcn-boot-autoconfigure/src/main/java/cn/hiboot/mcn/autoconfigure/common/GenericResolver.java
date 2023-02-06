package cn.hiboot.mcn.autoconfigure.common;

import org.springframework.core.ResolvableType;

/**
 * GenericResolver
 *
 * @author DingHao
 * @since 2023/2/6 10:09
 */
public interface GenericResolver<T> extends Resolver<T> {
    default boolean supportsType(Class<? extends T> type) {
        return supportsType(ResolvableType.forClass(type));
    }
    boolean supportsType(ResolvableType type);
}
