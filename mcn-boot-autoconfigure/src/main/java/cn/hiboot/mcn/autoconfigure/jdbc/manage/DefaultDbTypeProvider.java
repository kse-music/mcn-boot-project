package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.util.McnUtils;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * DefaultDbTypeProvider
 *
 * @author DingHao
 * @since 2025/11/11 9:57
 */
public class DefaultDbTypeProvider implements DbTypeProvider {

    private final String name;
    private final String driverClassName;
    private final String platform;
    private String quote = "\"";
    private String colon = ":";

    public DefaultDbTypeProvider(String name, String driverClassName) {
        this(name,driverClassName, null);
    }

    public DefaultDbTypeProvider(String name, String driverClassName, String platform) {
        this.name = name;
        this.driverClassName = driverClassName;
        this.platform = platform;
    }

    public DbTypeProvider quote(String quote) {
        this.quote = quote;
        return this;
    }

    public DbTypeProvider colon(String colon) {
        this.colon = colon;
        return this;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String driverClassName() {
        return this.driverClassName;
    }

    @Override
    public String platform() {
        return this.platform;
    }

    @Override
    public String url(ConnectConfig connectConfig) {
        String platform = this.platform() == null ? this.name() : this.platform();
        String url = "jdbc:" + platform + this.colon + "//" + connectConfig.getIp();
        Integer port = connectConfig.getPort();
        if (port != null) {
            url += ":" + port;
        }
        url = postProcessJdbcUrl(connectConfig, url);
        Map<String, Object> connectParameter = connectConfig.getConnectParameter();
        if (McnUtils.isNotNullAndEmpty(connectParameter)) {
            return appendConnectParameter(connectParameter, url);
        }
        return url;
    }

    protected String postProcessJdbcUrl(ConnectConfig connectConfig, String url) {
        if (McnUtils.isNotNullAndEmpty(connectConfig.getCatalog())) {
            url += "/" + connectConfig.getCatalog();
        }
        return url;
    }

    protected String appendConnectParameter(Map<String, Object> connectParameter, String url) {
        StringJoiner joiner = new StringJoiner("&", url + "?", "");
        for (Map.Entry<String, Object> entry : connectParameter.entrySet()) {
            joiner.add(entry.getKey() + "=" + entry.getValue());
        }
        return joiner.toString();
    }

    @Override
    public String pageSql(String sql, Integer skip, Integer limit) {
        return sql + " limit :skip,:pageSize";
    }

    @Override
    public String sqlQuote(String str) {
        return quote + str + quote;
    }

}