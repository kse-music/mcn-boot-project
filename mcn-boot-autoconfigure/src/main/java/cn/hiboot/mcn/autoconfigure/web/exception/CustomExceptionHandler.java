package cn.hiboot.mcn.autoconfigure.web.exception;

import javax.servlet.http.HttpServletRequest;

/**
 * 自定义异常处理
 *
 * @author DingHao
 * @since 2021/11/30 17:45
 */
public interface CustomExceptionHandler<T> {

    /**
     * 自定义异常处理
     * @param request httpRequest
     * @param t ex
     * @return ex result
     */
    T handle(HttpServletRequest request,Throwable t);

}
