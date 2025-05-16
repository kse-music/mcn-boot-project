package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import java.util.Objects;

/**
 * ConnectConfig
 *
 * @author DingHao
 * @since 2025/5/15 12:04
 */
public class ConnectConfig {

    private String ip;
    private Integer port;
    private String userName;
    private String password;
    private String dbType;

    private String catalog;
    /**
     * mysql is null, dm is db
     */
    private String schema;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConnectConfig that = (ConnectConfig) o;
        return Objects.equals(ip, that.ip) && Objects.equals(port, that.port) && Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port, userName);
    }

}
