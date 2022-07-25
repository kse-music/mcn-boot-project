package cn.hiboot.mcn.autoconfigure.mybatis;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.util.Map;

/**
 * MultipleDataSourceProperties
 *
 * @author DingHao
 * @since 2022/7/25 17:31
 */
public class MultipleDataSourceProperties {

    private Map<String, DataSourceProperties> datasource;

    public Map<String, DataSourceProperties> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<String, DataSourceProperties> datasource) {
        this.datasource = datasource;
    }

}
