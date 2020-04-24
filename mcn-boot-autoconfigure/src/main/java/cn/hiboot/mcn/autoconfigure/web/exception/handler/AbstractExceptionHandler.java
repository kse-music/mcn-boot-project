package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import cn.hiboot.mcn.autoconfigure.context.McnPropertiesPostProcessor;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * common
 *
 * @author DingHao
 * @since 2019/1/2 19:04
 */
public abstract class AbstractExceptionHandler extends ErrorMsg implements EnvironmentAware {

    protected String basePackage;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected void dealStackTraceElement(Exception exception){
        List<StackTraceElement> elements = Arrays.asList(exception.getStackTrace()).stream().filter(s -> s.getClassName().contains(basePackage)).collect(Collectors.toList());
        exception.setStackTrace(elements.toArray(new StackTraceElement[elements.size()]));
    }

    @Override
    public void setEnvironment(Environment environment) {
        basePackage = environment.getProperty(McnPropertiesPostProcessor.APP_BASE_PACKAGE);
    }

}
