package cn.hiboot.mcn.autoconfigure.web.reactor;

import cn.hiboot.mcn.autoconfigure.web.exception.AbstractExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.GlobalExceptionProperties;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Function;

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

    @Override
    protected Function<Throwable, Integer> customHandleException() {
        return ex -> {
            if(ex instanceof ResponseStatusException){
                return mappingCode((ResponseStatusException) ex);
            }
            return null;
        };
    }

    @Override
    protected String getMessage(Throwable t) {
        if(t instanceof ResponseStatusException){
            return ((ResponseStatusException) t).getReason();
        }
        return super.getMessage(t);
    }

    private int mappingCode(ResponseStatusException exception){
        if(isOverrideHttpError()){
            return ExceptionKeys.mappingCode(exception.getRawStatusCode());
        }
        return exception.getRawStatusCode();
    }

}
