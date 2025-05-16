package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.model.base.PageSort;

import java.util.List;

/**
 * DataQuery
 *
 * @author DingHao
 * @since 2025/5/15 17:41
 */
public class DataQuery extends PageSort {

    private String tableName;

    private List<FieldQuery> query;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<FieldQuery> getQuery() {
        return query;
    }

    public void setQuery(List<FieldQuery> query) {
        this.query = query;
    }

}
