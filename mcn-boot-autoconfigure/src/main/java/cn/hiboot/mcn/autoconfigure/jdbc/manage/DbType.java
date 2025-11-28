package cn.hiboot.mcn.autoconfigure.jdbc.manage;

/**
 * DbType
 *
 * @author DingHao
 * @since 2025/5/15 14:11
 */
public interface DbType {

    String name();

    String driverClassName();

    String platform();

    String url(ConnectConfig connectConfig);

    String pageSql(String sql, Integer skip, Integer limit);

    String sqlQuote(String str);

    default RdbManage rdbManage(RdbManage rdbManage) {
        return rdbManage;
    }

}
