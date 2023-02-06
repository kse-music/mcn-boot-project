package cn.hiboot.mcn.autoconfigure.web.exception;

import cn.hiboot.mcn.core.model.result.RestResp;

import javax.servlet.http.HttpServletRequest;

/**
 * 异常后置处理器
 *
 * @author DingHao
 * @since 2022/6/29 13:27
 */
public interface ExceptionPostProcessor<T> {

    /**
     * 在内部异常处理器之后执行
     * @param request 当前请求
     * @param t 异常
     * @param resp 内部异常处理器处理结果
     * @return 如果返回非null则直接结束异常处理的结果
     */
    default T afterHandle(HttpServletRequest request, Throwable t, RestResp<Throwable> resp){
        return afterHandle(resp);
    }

    T afterHandle(RestResp<Throwable> resp);
}
