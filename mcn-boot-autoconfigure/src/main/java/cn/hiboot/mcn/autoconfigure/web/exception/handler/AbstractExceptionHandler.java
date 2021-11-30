package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.context.McnPropertiesPostProcessor;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * common
 *
 * @author DingHao
 * @since 2019/1/2 19:04
 */
public abstract class AbstractExceptionHandler extends ErrorMsg implements EnvironmentAware {

    protected boolean setValidatorResult;
    private boolean removeFrameworkStack;
    private String basePackage;
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected void dealStackTraceElement(Throwable exception){
        if(removeFrameworkStack){
            exception.setStackTrace(Arrays.stream(exception.getStackTrace()).filter(s -> s.getClassName().contains(basePackage)).toArray(StackTraceElement[]::new));
        }
    }

    protected void logError(String msg,Throwable t){
        log.error("ErrorMsg = {}",msg,t);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.basePackage = environment.getProperty(McnPropertiesPostProcessor.APP_BASE_PACKAGE);
        this.removeFrameworkStack = environment.getProperty("remove.framework.stack.enable",Boolean.class,true);
        this.setValidatorResult = environment.getProperty("validator.result.set.enable",Boolean.class,true);
    }

}
