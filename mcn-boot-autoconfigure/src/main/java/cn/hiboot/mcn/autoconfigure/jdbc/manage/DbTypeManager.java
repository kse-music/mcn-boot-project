package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.util.McnUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DefaultDbTypeManager
 *
 * @author DingHao
 * @since 2025/11/11 9:44
 */
public abstract class DbTypeManager {

    private static final Map<String, DbTypeProvider> REGISTRY = new ConcurrentHashMap<>();

    static {
        registerDefault();
        ServiceLoader.load(DbTypeProvider.class).forEach(DbTypeManager::register);
    }

    private static void registerDefault() {
        register(
                new DefaultDbTypeProvider("mysql", "com.mysql.cj.jdbc.Driver").quote("`"),
                new DefaultDbTypeProvider("postgresql", "org.postgresql.Driver"),
                new DefaultDbTypeProvider("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver") {

                    @Override
                    protected String postProcessJdbcUrl(ConnectConfig connectConfig, String url) {
                        if (McnUtils.isNotNullAndEmpty(connectConfig.getCatalog())) {
                            url += ";databaseName=" + connectConfig.getCatalog();
                        }
                        return url;
                    }

                    @Override
                    protected String appendConnectParameter(Map<String, Object> connectParameter, String url) {
                        StringJoiner joiner = new StringJoiner(";", url + ";", "");
                        for (Map.Entry<String, Object> entry : connectParameter.entrySet()) {
                            joiner.add(entry.getKey() + "=" + entry.getValue());
                        }
                        return joiner.toString();
                    }

                },
                new DefaultDbTypeProvider("oracle", "oracle.jdbc.OracleDriver", "oracle:thin:") {
                    @Override
                    public String pageSql(String sql, Integer skip, Integer limit) {
                        return  "select * from ( select rownum rn , t.* from (" + sql + ") t where rownum <= " + (limit + skip) + ") tt where tt.rn > " + skip;
                    }
                }.colon("@"),
                new DefaultDbTypeProvider("mariadb", "org.mariadb.jdbc.Driver").quote("`"),
                new DefaultDbTypeProvider("dm", "dm.jdbc.driver.DmDriver") {
                    @Override
                    protected String postProcessJdbcUrl(ConnectConfig connectConfig, String url) {
                        if (McnUtils.isNotNullAndEmpty(connectConfig.getSchema())) {
                            url += "/" + connectConfig.getSchema();
                        }
                        return url;
                    }
                },
                new DefaultDbTypeProvider("kingbase", "com.kingbase8.Driver", "kingbase8")
        );
    }

    public static void register(DbTypeProvider... providers) {
        for (DbTypeProvider provider : providers) {
            REGISTRY.put(provider.name().toLowerCase(), provider);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends DbTypeProvider> T get(String name) {
        return (T) REGISTRY.get(name.toLowerCase());
    }

    public static Collection<DbTypeProvider> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

}
