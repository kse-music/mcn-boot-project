package cn.hiboot.mcn.autoconfigure.web.reactor;

import cn.hiboot.mcn.autoconfigure.web.exception.AbstractExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.GlobalExceptionProperties;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.web.server.ResponseStatusException;

/**
 * GlobalServerExceptionHandler
 *
 * @author DingHao
 * @since 2023/5/24 18:01
 */
public class GlobalServerExceptionHandler extends AbstractExceptionHandler {

    public GlobalServerExceptionHandler(GlobalExceptionProperties properties) {
        super(properties);
    }

    public RestResp<Throwable> handleException(Throwable exception,String additionMsg) {
        RestResp<Throwable> resp = super.handleException(exception);
        if(properties().isReturnOriginExMsg()){
            return resp;
        }
        resp.setErrorInfo(additionMsg + resp.getErrorInfo());
        return resp;
    }

    @Override
    protected Integer mappingCode(Throwable ex) {
        if(ex instanceof ResponseStatusException){
            return ExceptionKeys.mappingCode(((ResponseStatusException) ex).getStatus().value());
        }
        return super.mappingCode(ex);
    }

}
