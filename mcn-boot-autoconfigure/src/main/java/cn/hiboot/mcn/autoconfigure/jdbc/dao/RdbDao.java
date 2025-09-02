package cn.hiboot.mcn.autoconfigure.jdbc.dao;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * RdbDao
 *
 * @author DingHao
 * @since 2025/9/2 21:44
 */
public interface RdbDao {

    Long insert(String sql, Object... args);

    int update(String sql, Object... args);

    int delete(String sql, Object... args);

    void query(String sql, Consumer<Map<String, Object>> consumer);

    Map<String, Object> queryForMap(String sql, Object... args);

    List<Map<String, Object>> queryForList(String sql, Object... args);

    int count(String sql, Object... args);

    static RdbDao create(DataSource dataSource) {
        return new DefaultRdbDao(dataSource);
    }

}
