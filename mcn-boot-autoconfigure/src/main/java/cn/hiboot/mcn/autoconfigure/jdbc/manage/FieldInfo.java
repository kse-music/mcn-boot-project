package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * FieldInfo
 *
 * @author DingHao
 * @since 2025/5/15 17:10
 */
public class FieldInfo {

    private String catalog;
    private String tableSchema;
    private String tableName;
    private Integer dataType;
    private String columnName;
    private Integer columnSize;
    private String dataTypeName;
    private String remarks;

    public FieldInfo() {
    }

    public FieldInfo(ResultSet rs) throws SQLException {
        this.catalog = rs.getString("TABLE_CAT");
        this.tableSchema = rs.getString("TABLE_SCHEM");
        this.tableName = rs.getString("TABLE_NAME");
        this.dataType = rs.getInt("DATA_TYPE");
        this.columnName = rs.getString("COLUMN_NAME");
        this.columnSize = rs.getInt("COLUMN_SIZE");
        this.dataTypeName = rs.getString("TYPE_NAME");
        this.remarks = rs.getString("REMARKS");
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(Integer columnSize) {
        this.columnSize = columnSize;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
