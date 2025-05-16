package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ImportedKeyInfo
 *
 * @author DingHao
 * @since 2025/5/15 17:00
 */
public class ImportedKeyInfo {

    private String pkTableCat;
    private String pkTableSchema;
    private String pkTableName;
    private String pkColumnName;
    private String fkTableCat;
    private String fkTableSchema;
    private String fkTableName;
    private String fkColumnName;
    private String name;

    public ImportedKeyInfo() {
    }

    public ImportedKeyInfo(ResultSet rs) throws SQLException {
        this.pkTableCat = rs.getString("PKTABLE_CAT");
        this.pkTableSchema = rs.getString("PKTABLE_SCHEM");
        this.pkTableName = rs.getString("PKTABLE_NAME");
        this.pkColumnName = rs.getString("PKCOLUMN_NAME");
        this.fkTableCat = rs.getString("FKTABLE_CAT");
        this.fkTableSchema = rs.getString("FKTABLE_SCHEM");
        this.fkTableName = rs.getString("FKTABLE_NAME");
        this.fkColumnName = rs.getString("FKCOLUMN_NAME");
        this.name = rs.getString("FK_NAME");
    }

    public String getPkTableCat() {
        return pkTableCat;
    }

    public void setPkTableCat(String pkTableCat) {
        this.pkTableCat = pkTableCat;
    }

    public String getPkTableSchema() {
        return pkTableSchema;
    }

    public void setPkTableSchema(String pkTableSchema) {
        this.pkTableSchema = pkTableSchema;
    }

    public String getPkTableName() {
        return pkTableName;
    }

    public void setPkTableName(String pkTableName) {
        this.pkTableName = pkTableName;
    }

    public String getPkColumnName() {
        return pkColumnName;
    }

    public void setPkColumnName(String pkColumnName) {
        this.pkColumnName = pkColumnName;
    }

    public String getFkTableCat() {
        return fkTableCat;
    }

    public void setFkTableCat(String fkTableCat) {
        this.fkTableCat = fkTableCat;
    }

    public String getFkTableSchema() {
        return fkTableSchema;
    }

    public void setFkTableSchema(String fkTableSchema) {
        this.fkTableSchema = fkTableSchema;
    }

    public String getFkTableName() {
        return fkTableName;
    }

    public void setFkTableName(String fkTableName) {
        this.fkTableName = fkTableName;
    }

    public String getFkColumnName() {
        return fkColumnName;
    }

    public void setFkColumnName(String fkColumnName) {
        this.fkColumnName = fkColumnName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
