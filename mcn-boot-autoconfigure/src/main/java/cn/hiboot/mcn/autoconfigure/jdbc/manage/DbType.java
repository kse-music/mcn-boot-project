package cn.hiboot.mcn.autoconfigure.jdbc.manage;

/**
 * DbType
 *
 * @author DingHao
 * @since 2025/5/15 14:11
 */
public enum DbType {

    mysql("com.mysql.cj.jdbc.Driver"),
    oracle("oracle:thin:@", "oracle.jdbc.OracleDriver"),
    postgresql("org.postgresql.Driver"),
    sqlserver("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    mariadb("org.mariadb.jdbc.Driver"),
    dm("dm.jdbc.driver.DmDriver"),
    kingbase("kingbase8","com.kingbase8.Driver");

    private final String platform;
    private final String driverClassName;

    DbType(String driverClassName) {
        this(null, driverClassName);
    }

    DbType(String platform, String driverClassName) {
        this.platform = platform;
        this.driverClassName = driverClassName;
    }

    public String url(ConnectConfig connectConfig) {
        String platform = this.platform == null ? this.name() : this.platform;
        String colon = this == oracle ? "" : ":";
        String url = "jdbc:" + platform + colon + "//" + connectConfig.getIp() + ":" + connectConfig.getPort() + "/";
        if (this == dm) {
            url += connectConfig.getSchema();
        }else {
            url += connectConfig.getCatalog();
        }
        return url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String pageSql(String sql, Integer skip, Integer limit) {
        String pageSql;
        if (this == oracle) {
            pageSql = "select * from ( select rownum rn , t.* from (" + sql + ") t where rownum <= " + (limit + skip) + ") tt where tt.rn > " + skip;
        } else {
            pageSql = sql + " limit :skip,:pageSize";
        }
        return pageSql;
    }

    public String sqlQuote(String str) {
        String sqlQuote = "\"";
        if (this == mysql || this == mariadb) {
            sqlQuote = "`";
        }
        return sqlQuote + str + sqlQuote;
    }

}
