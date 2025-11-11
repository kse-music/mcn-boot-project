package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.util.McnUtils;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

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

    default boolean isOracle() {
        return Objects.equals(this.name(), "oracle");
    }

    default boolean isDm() {
        return Objects.equals(this.name(), "dm");
    }

    default boolean isMysql() {
        return Objects.equals(this.name(), "mysql") || Objects.equals(this.name(), "mariadb");
    }

    default String url(ConnectConfig connectConfig) {
        String platform = this.platform() == null ? this.name() : this.platform();
        String colon = isOracle() ? "" : ":";
        String url = "jdbc:" + platform + colon + "//" + connectConfig.getIp() + ":" + connectConfig.getPort() + "/";
        if (isDm()) {
            if (McnUtils.isNotNullAndEmpty(connectConfig.getSchema())) {
                url += connectConfig.getSchema();
            }
        } else {
            if (McnUtils.isNotNullAndEmpty(connectConfig.getCatalog())) {
                url += connectConfig.getCatalog();
            }
        }
        Map<String, Object> connectParameter = connectConfig.getConnectParameter();
        if (McnUtils.isNotNullAndEmpty(connectParameter)) {
            StringJoiner joiner = new StringJoiner("&", url + "?", "");
            for (Map.Entry<String, Object> entry : connectParameter.entrySet()) {
                joiner.add(entry.getKey() + "=" + entry.getValue());
            }
            url = joiner.toString();
        }
        return url;
    }

    default String pageSql(String sql, Integer skip, Integer limit) {
        String pageSql;
        if (isOracle()) {
            pageSql = "select * from ( select rownum rn , t.* from (" + sql + ") t where rownum <= " + (limit + skip) + ") tt where tt.rn > " + skip;
        } else {
            pageSql = sql + " limit :skip,:pageSize";
        }
        return pageSql;
    }

    default String sqlQuote(String str) {
        String sqlQuote = "\"";
        if (isMysql()) {
            sqlQuote = "`";
        }
        return sqlQuote + str + sqlQuote;
    }

}
