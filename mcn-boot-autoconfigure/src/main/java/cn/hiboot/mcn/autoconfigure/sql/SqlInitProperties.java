package cn.hiboot.mcn.autoconfigure.sql;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SqlInitProperties
 *
 * @author DingHao
 * @since 2022/10/10 14:20
 */
@ConfigurationProperties("spring.sql.init.additional")
public class SqlInitProperties {

    private boolean enabled = false;
    private String separator = "//";
    private String dir = "db";
    /**
     * 用于建立连接再创建应用所使用的db
     */
    private String initDbName;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getInitDbName() {
        return initDbName;
    }

    public void setInitDbName(String initDbName) {
        this.initDbName = initDbName;
    }
}
