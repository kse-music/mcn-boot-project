package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.model.result.RestResp;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * RdbManageImpl
 *
 * @author DingHao
 * @since 2025/11/28 16:57
 */
class RdbManageImpl implements RdbManage {

    static final RdbManage defaultRdbManage = new RdbManageImpl();

    private static final RdbManage rdbManage = new DefaultRdbManage();

    public RdbManage delegate(ConnectConfig connectConfig) {
        return connectConfig.dbType().rdbManage(rdbManage);
    }

    @Override
    public void connect(ConnectConfig connectConfig, Consumer<Connection> consumer) {
        delegate(connectConfig).connect(connectConfig, consumer);
    }

    @Override
    public List<SchemaInfo> schemaInfo(ConnectConfig connectConfig, DbQuery dbQuery) {
        return delegate(connectConfig).schemaInfo(connectConfig, dbQuery);
    }

    @Override
    public List<TableInfo> tableInfo(ConnectConfig connectConfig, DbQuery dbQuery) {
        return delegate(connectConfig).tableInfo(connectConfig, dbQuery);
    }

    @Override
    public List<FieldInfo> findFieldInfo(ConnectConfig connectConfig, DbQuery dbQuery) {
        return delegate(connectConfig).findFieldInfo(connectConfig, dbQuery);
    }

    @Override
    public List<ImportedKeyInfo> findImportedKeys(ConnectConfig connectConfig, DbQuery dbQuery) {
        return delegate(connectConfig).findImportedKeys(connectConfig, dbQuery);
    }

    @Override
    public List<Map<String, Object>> listData(ConnectConfig connectConfig, DataQuery dataQuery) {
        return delegate(connectConfig).listData(connectConfig, dataQuery);
    }

    @Override
    public Long countData(ConnectConfig connectConfig, DataQuery dataQuery) {
        return delegate(connectConfig).countData(connectConfig, dataQuery);
    }

    @Override
    public RestResp<List<Map<String, Object>>> pageData(ConnectConfig connectConfig, DataQuery dataQuery) {
        return delegate(connectConfig).pageData(connectConfig, dataQuery);
    }

    @Override
    public int insert(ConnectConfig connectConfig, String tableName, Map<String, Object> data) {
        return delegate(connectConfig).insert(connectConfig, tableName, data);
    }

    @Override
    public int update(ConnectConfig connectConfig, String tableName, Map<String, Object> data, String condition, Map<String, Object> params) {
        return delegate(connectConfig).update(connectConfig, tableName, data, condition, params);
    }

    @Override
    public int delete(ConnectConfig connectConfig, String tableName, String condition, Map<String, Object> params) {
        return delegate(connectConfig).delete(connectConfig, tableName, condition, params);
    }

}
