package cn.hiboot.mcn.autoconfigure.web.exception;

import cn.hiboot.mcn.core.model.result.RestResp;

/**
 * RestRespCustomizer
 *
 * @author DingHao
 * @since 2022/6/8 14:24
 */
public interface RestRespCustomizer<T> {

    T custom(RestResp<Object> resp);

}
