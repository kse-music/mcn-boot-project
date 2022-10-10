package cn.hiboot.mcn.autoconfigure.sql;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * SqlInitProperties
 *
 * @author DingHao
 * @since 2022/10/10 14:20
 */
@ConfigurationProperties("spring.sql.init.additional")
public class SqlInitProperties {

    private String separator = "//";
    private List<String> scriptLocations;

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public List<String> getScriptLocations() {
        return scriptLocations;
    }

    public void setScriptLocations(List<String> scriptLocations) {
        this.scriptLocations = scriptLocations;
    }
}
