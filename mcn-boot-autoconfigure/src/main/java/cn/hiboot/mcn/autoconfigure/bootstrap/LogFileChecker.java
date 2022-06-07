package cn.hiboot.mcn.autoconfigure.bootstrap;

import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * LogFileChecker
 *
 * @author DingHao
 * @since 2021/12/25 16:46
 */
public class LogFileChecker {

    private final String originalLogFile;
    private final ConfigurableEnvironment environment;

    public LogFileChecker(ConfigurableEnvironment environment) {
        this.environment = environment;
        this.originalLogFile = getLogFile(environment);
    }

    public void check(){
        String finalLogFile = getLogFile(environment);
        if(Objects.nonNull(finalLogFile) && !finalLogFile.equals(originalLogFile)){
            try {
                Files.delete(Paths.get(originalLogFile));
            } catch (IOException e) {
                //ignore
            }
        }
    }

    private String getLogFile(ConfigurableEnvironment environment ) {
        return environment.getProperty("logging.file.name");
    }

}
