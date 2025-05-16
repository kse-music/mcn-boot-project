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
        return rdbManageMap.computeIfAbsent(config, k -> new DataSourceManage(config));
    }

    @Override
    public void connect(ConnectConfig connectConfig, Consumer<Connection> consumer) {
        rdbMetaDataManage(connectConfig).withConnection(connection -> {
            consumer.accept(connection);
            return null;
        });
    }

    @Override
    public List<TableInfo> tableInfo(ConnectConfig connectConfig, DbQuery dbQuery) {
        return rdbMetaDataManage(connectConfig).withConnection(connection -> {
            List<TableInfo> result = new ArrayList<>();
            DatabaseMetaData metaData = connection.getMetaData();
            DbQuery dq = buildDbQuery(connectConfig, dbQuery);
            ResultSet rs = metaData.getTables(dq.getCatalog(), dq.getTableSchema(), dq.getTableName(), dq.types());
            while (rs.next()) {
                result.add(new TableInfo(rs));
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
            tableNamePattern += dq.getTableName() + "%";
        }
        if (dq.getColumnNamePattern() != null) {
            columnNamePattern += dq.getColumnNamePattern() + "%";
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
            DatabaseMetaData metaData = connection.getMetaData();
            DbQuery dq = buildDbQuery(connectConfig, dbQuery);
            return fieldInfo(metaData, dq.getCatalog(), dq.getTableSchema(), dq.getTableName(), dq.getColumnNamePattern());
        });
    }

    private List<FieldInfo> fieldInfo(DatabaseMetaData metaData,String catalog, String schemaPattern,
                                      String tableNamePattern, String columnNamePattern) throws SQLException {
        List<FieldInfo> result = new ArrayList<>();
        ResultSet resultSet = metaData.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
        while (resultSet.next()) {
            result.add(new FieldInfo(resultSet));
        }
        return result;
    }

    @Override
    public List<ImportedKeyInfo> findImportedKeys(ConnectConfig connectConfig, DbQuery dbQuery) {
        return rdbMetaDataManage(connectConfig).withConnection(connection -> {
            List<ImportedKeyInfo> result = new ArrayList<>();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getImportedKeys(connection.getCatalog(), dbQuery.getTableSchema(), dbQuery.getTableName());
            while (rs.next()) {
                result.add(new ImportedKeyInfo(rs));
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
        List<FieldInfo> fieldInfo = dataSourceManage.withConnection(connection -> {
            DatabaseMetaData metaData = connection.getMetaData();
            return fieldInfo(metaData, connectConfig.getCatalog(), connectConfig.getSchema(), dataQuery.getTableName(), "%");
        });
        Map<String, Object> paramMap = new HashMap<>();
        String condition = RdbManageUtil.buildCondition(connectConfig, dataQuery, fieldInfo, paramMap);
        String tableName = fromTable(connectConfig, dataQuery.getTableName());
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = dataSourceManage.namedParameterJdbcTemplate();
        RestResp<List<Map<String, Object>>> result = RestResp.ok();
        if (data) {
            String sql = "SELECT * FROM " + tableName + condition + RdbManageUtil.buildSort(dataQuery);
            Integer skip = dataQuery.getSkip();
            Integer limit = dataQuery.getLimit();
            if (skip != null || limit != null) {
                if (skip == null || skip < 0) {
                    skip = 0;
                }
                if (limit == null || limit < 0) {
                    limit = 10;
                }
                paramMap.put("skip", skip);
                paramMap.put("pageSize", limit);
                sql += " limit :skip,:pageSize";
            }
            result.setData(namedParameterJdbcTemplate.queryForList(sql, paramMap).stream().map(RdbManageUtil::tranMap).collect(Collectors.toList()));
        }
        if (count) {
            String sqlCount = "SELECT count(*) FROM " + tableName + condition;
            result.setCount(namedParameterJdbcTemplate.queryForObject(sqlCount, paramMap, Long.class));
        }
        return result;
    }

    private String fromTable(ConnectConfig connectConfig, String tableName) {
        return DbType.fromTable(connectConfig.getDbType(), connectConfig.getSchema(), tableName);
    }

}
