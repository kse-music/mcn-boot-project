package cn.hiboot.mcn.autoconfigure.web.exception;

/**
 * 异常错误消息处理器
 *
 * @author DingHao
 * @since 2022/6/29 12:01
 */
public interface ExceptionMessageProcessor {
    String process(int errorCode);
}
