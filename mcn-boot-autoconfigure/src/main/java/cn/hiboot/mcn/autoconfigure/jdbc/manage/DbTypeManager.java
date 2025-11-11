package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import java.util.Collection;

/**
 * DbTypeManager
 *
 * @author DingHao
 * @since 2025/11/11 9:44
 */
public interface DbTypeManager {

    void register(DbTypeProvider... providers);

    DbTypeProvider get(String name);

    Collection<DbTypeProvider> all();

    static DbTypeManager defaults() {
        return DefaultDbTypeManager.defaults;
    }

}
