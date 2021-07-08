package cn.hiboot.mcn.core.exception;

import cn.hiboot.mcn.core.model.result.RestResp;

/**
 * 未捕获异常处理器
 *
 * @author DingHao
 * @since 2021/7/8 10:10
 */
public interface UnCaughtExceptionHandler<E extends Exception> {

    void handle(RestResp<Object> errorInfo, E e);

}
