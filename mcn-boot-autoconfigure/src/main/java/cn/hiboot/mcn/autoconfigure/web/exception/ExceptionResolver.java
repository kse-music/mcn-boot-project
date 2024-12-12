package cn.hiboot.mcn.autoconfigure.web.exception;

import cn.hiboot.mcn.core.model.result.RestResp;

/**
 * 异常解析器
 *
 * @author DingHao
 * @since 2022/6/25 22:26
 */
public interface ExceptionResolver<T extends Throwable> {

    RestResp<T> resolve(T t);

}
