package cn.hiboot.mcn.autoconfigure.common;

import cn.hiboot.mcn.core.model.result.RestResp;

/**
 * Resolver
 *
 * @author DingHao
 * @since 2023/2/6 10:09
 */
public interface Resolver<T> {
    RestResp<T> resolve(T t);
}
