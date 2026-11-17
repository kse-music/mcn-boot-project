package cn.hiboot.mcn.docs;

import cn.hiboot.mcn.autoconfigure.jdbc.DataSourceHolder;
import cn.hiboot.mcn.autoconfigure.jdbc.SwitchSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class RoutingExamples {
    private final JdbcTemplate jdbcTemplate;

    public RoutingExamples(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @SwitchSource("world")
    public List<Map<String, Object>> listUsers() {
        return jdbcTemplate.queryForList("select id, name from t_user");
    }

    public static <T> T withDataSource(String name, Supplier<T> operation) {
        String previous = DataSourceHolder.getDataSource();
        DataSourceHolder.setDataSource(name);
        try {
            return operation.get();
        } finally {
            if (previous == null) {
                DataSourceHolder.clearDataSource();
            } else {
                DataSourceHolder.setDataSource(previous);
            }
        }
    }
}
