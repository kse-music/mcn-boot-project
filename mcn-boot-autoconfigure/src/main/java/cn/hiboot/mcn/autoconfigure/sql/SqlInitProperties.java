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
    private String dir = "db";
    private List<String> locations;
    /**
     * 用于建立连接再创建应用所使用的db
     */
    private String initDbName;

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

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public String getInitDbName() {
        return initDbName;
    }

    public void setInitDbName(String initDbName) {
        this.initDbName = initDbName;
    }
}
