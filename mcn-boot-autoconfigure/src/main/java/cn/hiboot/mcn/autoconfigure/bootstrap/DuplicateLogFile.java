package cn.hiboot.mcn.autoconfigure.bootstrap;

import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * DuplicateLogFile
 *
 * @author DingHao
 * @since 2021/12/25 16:46
 */
public class DuplicateLogFile{

    private String originalLogFile;
    private String finalLogFile;
    private boolean check;

    public void check(){
        if(Objects.nonNull(finalLogFile) && !finalLogFile.equals(originalLogFile) && check){
            try {
                Files.delete(Paths.get(originalLogFile));
            } catch (IOException e) {
                //ignore
            }
        }
    }

    public void setOriginalLogFile(ConfigurableEnvironment environment ) {
        this.originalLogFile = getLogFile(environment);
    }

    private String getLogFile(ConfigurableEnvironment environment ) {
        return environment.getProperty("logging.file.name");
    }

    public void setFinalLogFile(ConfigurableEnvironment environment ) {
        this.finalLogFile = getLogFile(environment);
        this.check = environment.getProperty("delete.default.log-file.enable", Boolean.class, true);
    }
}
