package cn.hiboot.mcn.autoconfigure.web.groovy;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GroovyWebProperties
 *
 * @author DingHao
 * @since 2021/12/22 17:05
 */
@ConfigurationProperties("groovy.web")
public class GroovyWebProperties {

    /**
     * 是否启用groovy web自动配置
     */
    private boolean enabled;
    private String sourceEncoding = "UTF-8";
    private String classpath;
    private boolean debug;
    private String scriptBaseClass;
    private String targetDirectory;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSourceEncoding() {
        return sourceEncoding;
    }

    public void setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getScriptBaseClass() {
        return scriptBaseClass;
    }

    public void setScriptBaseClass(String scriptBaseClass) {
        this.scriptBaseClass = scriptBaseClass;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }
}
