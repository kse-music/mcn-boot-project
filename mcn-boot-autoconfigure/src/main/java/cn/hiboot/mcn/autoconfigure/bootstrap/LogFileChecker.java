package cn.hiboot.mcn.autoconfigure.bootstrap;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        if(environment.getProperty("delete.default.log-file.enable", Boolean.class, true)){
            if(ObjectUtils.nullSafeEquals(getLogFile(environment),originalLogFile)){
                return;
            }
            try {
                Files.delete(Paths.get(originalLogFile));
            } catch (IOException e) {
                //ignore
            }
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
