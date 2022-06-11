package cn.hiboot.mcn.autoconfigure.web.exception;

import javax.servlet.http.HttpServletRequest;

/**
 * 自定义异常消息提示
 *
 * @author DingHao
 * @since 2021/11/30 17:45
 */
public interface ExceptionMessageCustomizer{

    String handle(Throwable t);

    default String handle(HttpServletRequest request,Throwable t){
        return handle(t);
    }

}
