package cn.hiboot.mcn.autoconfigure.jdbc;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.util.Map;

/**
 * MultipleDataSourceMarker
 *
 * @author DingHao
 * @since 2022/7/28 17:41
 */
public class MultipleDataSourceMarker {

    private Map<String, DataSourceProperties> properties;

    public MultipleDataSourceMarker(Map<String, DataSourceProperties> properties) {
        this.properties = properties;
    }

    public Map<String, DataSourceProperties> getProperties() {
        return properties;
    }
}
