package cn.hiboot.mcn.core.exception;

/**
 * 自定义异常消息提示
 *
 * @author DingHao
 * @since 2021/11/30 17:45
 */
public interface ExceptionMessageCustomizer{

    String handle(Throwable t);

}
