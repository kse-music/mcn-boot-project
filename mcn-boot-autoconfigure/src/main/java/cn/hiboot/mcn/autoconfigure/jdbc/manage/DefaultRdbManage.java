package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * DefaultRdbManage
 *
 * @author DingHao
 * @since 2025/5/15 15:01
 */
class DefaultRdbManage implements RdbManage {

    private static final Map<ConnectConfig, DataSourceManage> rdbManageMap = new ConcurrentHashMap<>();

    private DataSourceManage rdbMetaDataManage(ConnectConfig config) {
        if (rdbManageMap.containsKey(config)) {
            return rdbManageMap.get(config);
        }
        DataSourceManage dataSourceManage = new DataSourceManage(config);
        try {
            dataSourceManage.withConnection(connection -> null);
        } catch (Exception e) {
            throw new RuntimeException("connect error", e);
        }
        rdbManageMap.put(config, dataSourceManage);
        return dataSourceManage;
    }

    @Override
    public void connect(ConnectConfig connectConfig, Consumer<Connection> consumer) {
        rdbMetaDataManage(connectConfig).withConnection(connection -> {
            consumer.accept(connection);
            return null;
        });
    }

    @Override
    public List<SchemaInfo> schemaInfo(ConnectConfig connectConfig, DbQuery dbQuery) {
        return rdbMetaDataManage(connectConfig).withConnection(connection -> {
            List<SchemaInfo> result = new ArrayList<>();
            DbQuery dq = buildDbQuery(connectConfig, dbQuery);
            try (ResultSet rs = connection.getMetaData().getSchemas(dq.getCatalog(), dq.getTableSchema())) {
                while (rs.next()) {
                    result.add(new SchemaInfo(rs));
                }
            }
            return result;
        });
    }

    @Override
    public List<TableInfo> tableInfo(ConnectConfig connectConfig, DbQuery dbQuery) {
        return rdbMetaDataManage(connectConfig).withConnection(connection -> {
            List<TableInfo> result = new ArrayList<>();
            DbQuery dq = buildDbQuery(connectConfig, dbQuery);
            try (ResultSet rs = connection.getMetaData().getTables(dq.getCatalog(), dq.getTableSchema(), dq.getTableName(), dq.types())) {
                while (rs.next()) {
                    result.add(new TableInfo(rs));
                }
            }
            return result;
        });
    }

    private DbQuery buildDbQuery(ConnectConfig connectConfig, DbQuery dq) {
        String catalog = connectConfig.getCatalog();
        String tableSchema = connectConfig.getSchema();
        String tableNamePattern = "%";
        String columnNamePattern = "%";
        if (dq.getCatalog() != null) {
            catalog = dq.getCatalog();
        }
        if (dq.getTableSchema() != null) {
            tableSchema = dq.getTableSchema();
        }
        if (dq.getTableName() != null) {
            tableNamePattern = dq.getTableName();
        }
        if (dq.getColumnNamePattern() != null) {
            columnNamePattern = dq.getColumnNamePattern();
        }
        DbQuery dbQuery = new DbQuery();
        dbQuery.setCatalog(catalog);
        dbQuery.setTableSchema(tableSchema);
        dbQuery.setTableName(tableNamePattern);
        dbQuery.setColumnNamePattern(columnNamePattern);
        dbQuery.setTableType(dq.getTableType());
        return dbQuery;
    }

    @Override
    public List<FieldInfo> findFieldInfo(ConnectConfig connectConfig, DbQuery dbQuery) {
        return rdbMetaDataManage(connectConfig).withConnection(connection -> {
            DbQuery dq = buildDbQuery(connectConfig, dbQuery);
            return fieldInfo(connection.getMetaData(), dq.getCatalog(), dq.getTableSchema(), dq.getTableName(), dq.getColumnNamePattern());
        });
    }

    private List<FieldInfo> fieldInfo(DatabaseMetaData metaData,String catalog, String schemaPattern,
                                      String tableNamePattern, String columnNamePattern) throws SQLException {
        List<FieldInfo> result = new ArrayList<>();
        try (ResultSet resultSet = metaData.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern)) {
            while (resultSet.next()) {
                result.add(new FieldInfo(resultSet));
            }
        }
        return result;
    }

    @Override
    public List<ImportedKeyInfo> findImportedKeys(ConnectConfig connectConfig, DbQuery dbQuery) {
        return rdbMetaDataManage(connectConfig).withConnection(connection -> {
            List<ImportedKeyInfo> result = new ArrayList<>();
            try (ResultSet rs = connection.getMetaData().getImportedKeys(connection.getCatalog(), dbQuery.getTableSchema(), dbQuery.getTableName())) {
                while (rs.next()) {
                    result.add(new ImportedKeyInfo(rs));
                }
            }
            return result;
        });
    }

    @Override
    public List<Map<String, Object>> listData(ConnectConfig connectConfig, DataQuery dataQuery) {
        dataQuery.setSkip(null);
        dataQuery.setLimit(null);
        return queryData(connectConfig, dataQuery, true, false).getData();
    }

    @Override
    public Long countData(ConnectConfig connectConfig, DataQuery dataQuery) {
        return queryData(connectConfig, dataQuery, false, true).getCount();
    }

    @Override
    public RestResp<List<Map<String, Object>>> pageData(ConnectConfig connectConfig, DataQuery dataQuery) {
        return queryData(connectConfig, dataQuery, true, true);
    }

    private RestResp<List<Map<String, Object>>> queryData(ConnectConfig connectConfig, DataQuery dataQuery, boolean data, boolean count) {
        DataSourceManage dataSourceManage = rdbMetaDataManage(connectConfig);
        Map<String, Object> paramMap = new HashMap<>();
        DbQuery dq = buildDbQuery(connectConfig, dataQuery);
        String condition = RdbManageUtil.buildCondition(connectConfig, dataQuery, paramMap);
        String tableName = fromTable(connectConfig.dbType(), dq, dataQuery.getTableName());
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = dataSourceManage.namedParameterJdbcTemplate();
        RestResp<List<Map<String, Object>>> result = RestResp.ok();
        if (data) {
            String sql = "SELECT * FROM " + tableName + condition + RdbManageUtil.buildSort(dataQuery);
            Integer skip = dataQuery.getSkip();
            Integer limit = dataQuery.getLimit();
            boolean isOracle = connectConfig.dbType() == DbType.oracle;
            if (skip != null || limit != null) {
                if (skip == null || skip < 0) {
                    skip = 0;
                }
                if (limit == null || limit < 0) {
                    limit = 10;
                }
                paramMap.put("skip", skip);
                paramMap.put("pageSize", limit);
                sql = connectConfig.dbType().pageSql(sql, skip, limit);
            }
            result.setData(namedParameterJdbcTemplate.queryForList(sql, paramMap).stream().map(d -> {
                if (isOracle) {
                    d.remove("RN");
                }
                return RdbManageUtil.tranMap(d);
            }).collect(Collectors.toList()));
        }
        if (count) {
            String sqlCount = "SELECT count(*) FROM " + tableName + condition;
            result.setCount(namedParameterJdbcTemplate.queryForObject(sqlCount, paramMap, Long.class));
        }
        return result;
    }

    private String fromTable(DbType dbType, DbQuery dq, String tableName) {
        String schema = dq.getTableSchema();
        tableName = dbType.sqlQuote(tableName);
        if (schema != null) {
            return dbType.sqlQuote(schema) + "." + tableName;
        }
        return dbType.sqlQuote(dq.getCatalog()) + "." + tableName;
    }

}
