package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionProperties;
import cn.hiboot.mcn.core.model.result.RestResp;

/**
 * DefaultExceptionHandler
 *
 * @author DingHao
 * @since 2023/6/17 23:12
 */
public interface ExceptionHandler{

    String EXCEPTION_HANDLE_RESULT_ATTRIBUTE = ExceptionHandler.class.getName() + ".Error";

    ExceptionProperties config();

    RestResp<Throwable> handleException(Throwable exception);

    void handleError(Error error);

    void logError(Throwable t);

}
