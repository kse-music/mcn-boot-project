package cn.hiboot.mcn.autoconfigure.sql;

import org.springframework.boot.sql.init.DatabaseInitializationSettings;

import java.util.List;

/**
 * CustomDatabaseInitializationSettings
 *
 * @author DingHao
 * @since 2023/11/15 16:28
 */
class CustomDatabaseInitializationSettings extends DatabaseInitializationSettings {
    private List<String> scriptLocations;
    private String otherSeparator = ";";
    private String initDdName;
    private String platform;

    public List<String> getScriptLocations() {
        return scriptLocations;
    }

    public void setScriptLocations(List<String> scriptLocations) {
        this.scriptLocations = scriptLocations;
    }

    public String getOtherSeparator() {
        return otherSeparator;
    }

    public void setOtherSeparator(String otherSeparator) {
        this.otherSeparator = otherSeparator;
    }

    public String getInitDdName() {
        return initDdName;
    }

    public void setInitDdName(String initDdName) {
        this.initDdName = initDdName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
