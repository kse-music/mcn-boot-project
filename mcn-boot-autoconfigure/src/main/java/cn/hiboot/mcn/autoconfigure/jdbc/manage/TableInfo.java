package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TableInfo
 *
 * @author DingHao
 * @since 2025/5/15 15:33
 */
public class TableInfo {

    private String catalog;
    private String schema;
    private String name;
    private String type;
    private String remarks;

    public TableInfo() {
    }

    public TableInfo(ResultSet rs) throws SQLException {
        setCatalog(rs.getString("TABLE_CAT"));
        setSchema(rs.getString("TABLE_SCHEM"));
        setName(rs.getString("TABLE_NAME"));
        setType(rs.getString("TABLE_TYPE"));
        setRemarks(rs.getString("REMARKS"));
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

}