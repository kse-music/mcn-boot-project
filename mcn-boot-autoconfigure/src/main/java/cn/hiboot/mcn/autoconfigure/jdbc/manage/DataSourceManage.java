package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * RdbMetaDataManage
 *
 * @author DingHao
 * @since 2025/5/15 10:38
 */
class DataSourceManage {

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DataSourceManage(ConnectConfig connectConfig) {
        DbType dbType = connectConfig.dbType();
        this.dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(dbType.getDriverClassName())
                .url(dbType.url(connectConfig))
                .username(connectConfig.getUserName())
                .password(connectConfig.getPassword()).build();
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.dataSource);
    }

    <T> T withConnection(CheckedFunction<Connection, T> function) {
        try (Connection connection = dataSource.getConnection()) {
            return function.apply(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    JdbcTemplate jdbcTemplate() {
        return this.namedParameterJdbcTemplate.getJdbcTemplate();
    }

    NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return this.namedParameterJdbcTemplate;
    }

    interface CheckedFunction<T, R> {
        R apply(T t) throws SQLException;
    }

}
