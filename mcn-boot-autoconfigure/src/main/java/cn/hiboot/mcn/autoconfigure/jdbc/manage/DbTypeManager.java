package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
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
        register(new DbTypeModel("mysql", "com.mysql.cj.jdbc.Driver"),
                new DbTypeModel("postgresql", "org.postgresql.Driver"),
                new DbTypeModel("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
                new DbTypeModel("oracle", "oracle.jdbc.OracleDriver", "oracle:thin:@"),
                new DbTypeModel("mariadb", "org.mariadb.jdbc.Driver"),
                new DbTypeModel("dm", "dm.jdbc.driver.DmDriver"),
                new DbTypeModel("kingbase", "com.kingbase8.Driver", "kingbase8"));
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
