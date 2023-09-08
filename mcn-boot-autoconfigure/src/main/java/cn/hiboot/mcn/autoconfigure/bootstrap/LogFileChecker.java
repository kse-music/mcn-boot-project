package cn.hiboot.mcn.autoconfigure.bootstrap;

import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ObjectUtils;

/**
 * LogFileChecker
 *
 * @author DingHao
 * @since 2021/12/25 16:46
 */
public class LogFileChecker {

    private String originalLogFile;
    private ConfigurableEnvironment environment;

    public void check(){
        if(environment.getProperty("delete.default.log-file.enabled", Boolean.class, true)){
            if(ObjectUtils.nullSafeEquals(getLogFile(environment),originalLogFile)){
                return;
            }
            McnUtils.deleteFile(originalLogFile);
        }
    }

    public void setEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;
        this.originalLogFile = getLogFile(environment);
    }

    private String getLogFile(ConfigurableEnvironment environment ) {
        return environment.getProperty("logging.file.name");
    }

}