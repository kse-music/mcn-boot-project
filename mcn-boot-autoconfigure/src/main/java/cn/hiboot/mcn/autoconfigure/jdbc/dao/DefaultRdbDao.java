package cn.hiboot.mcn.autoconfigure.jdbc.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * DefaultRdbDao
 *
 * @author DingHao
 * @since 2025/9/2 21:44
 */
class DefaultRdbDao implements RdbDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DefaultRdbDao(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Long insert(String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return (key != null) ? key.longValue() : null;
    }

    @Override
    public int update(String sql, Object... args) {
        return jdbcTemplate.update(sql, args);
    }

    @Override
    public int delete(String sql, Object... args) {
        return jdbcTemplate.update(sql, args);
    }

    @Override
    public void query(String sql, Consumer<Map<String, Object>> consumer) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    sql,
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY
            )) {
                ps.setFetchSize(Integer.MIN_VALUE); // MySQL 流式
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }
                        consumer.accept(row);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... args) {
        List<Map<String, Object>> list = queryForList(sql, args);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    @Override
    public int count(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }

}
