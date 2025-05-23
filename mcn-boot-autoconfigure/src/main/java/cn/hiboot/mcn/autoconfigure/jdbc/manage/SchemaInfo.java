package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SchemaInfo
 *
 * @author DingHao
 * @since 2025/5/22 10:56
 */
public class SchemaInfo {

    private String catalog;
    private String schema;

    public SchemaInfo() {
    }

    public SchemaInfo(ResultSet rs) throws SQLException {
        try {
            setCatalog(rs.getString("TABLE_CATALOG"));
        } catch (Exception ignore) {

        }
        setSchema(rs.getString("TABLE_SCHEM"));
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

}
