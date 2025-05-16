package cn.hiboot.mcn.autoconfigure.jdbc.manage;

/**
 * DbQuery
 *
 * @author DingHao
 * @since 2025/5/15 15:47
 */
public class DbQuery {

    private String catalog;
    private String tableSchema;
    private String tableName;
    private String tableType;
    private String columnNamePattern;

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

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getColumnNamePattern() {
        return columnNamePattern;
    }

    public void setColumnNamePattern(String columnNamePattern) {
        this.columnNamePattern = columnNamePattern;
    }

    public String[] types() {
        if (getTableType() != null) {
            return new String[]{getTableType()};
        }
        return new String[]{"TABLE", "VIEW"};
    }

}
