package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.model.result.RestResp;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * DelegateRdbManage
 *
 * @author DingHao
 * @since 2025/5/15 15:01
 */
public class DelegateRdbManage implements RdbManage {

    private final RdbManage delegate;

    public DelegateRdbManage(RdbManage delegate) {
        this.delegate = delegate;
    }

    @Override
    public void connect(ConnectConfig connectConfig, Consumer<Connection> consumer) {
        delegate.connect(connectConfig, consumer);
    }

    @Override
    public List<SchemaInfo> schemaInfo(ConnectConfig connectConfig, DbQuery dbQuery) {
        return delegate.schemaInfo(connectConfig, dbQuery);
    }

    @Override
    public List<TableInfo> tableInfo(ConnectConfig connectConfig, DbQuery dbQuery) {
        return delegate.tableInfo(connectConfig, dbQuery);
    }

    @Override
    public List<FieldInfo> findFieldInfo(ConnectConfig connectConfig, DbQuery dbQuery) {
        return delegate.findFieldInfo(connectConfig, dbQuery);
    }

    @Override
    public List<ImportedKeyInfo> findImportedKeys(ConnectConfig connectConfig, DbQuery dbQuery) {
        return delegate.findImportedKeys(connectConfig, dbQuery);
    }

    @Override
    public List<Map<String, Object>> listData(ConnectConfig connectConfig, DataQuery dataQuery) {
        return delegate.listData(connectConfig, dataQuery);
    }

    @Override
    public Long countData(ConnectConfig connectConfig, DataQuery dataQuery) {
        return delegate.countData(connectConfig, dataQuery);
    }

    @Override
    public RestResp<List<Map<String, Object>>> pageData(ConnectConfig connectConfig, DataQuery dataQuery) {
        return delegate.pageData(connectConfig, dataQuery);
    }

    @Override
    public int insert(ConnectConfig connectConfig, String tableName, Map<String, Object> data) {
        return delegate.insert(connectConfig, tableName, data);
    }

    @Override
    public int update(ConnectConfig connectConfig, String tableName, Map<String, Object> data, String condition, Map<String, Object> params) {
        return delegate.update(connectConfig, tableName, data, condition, params);
    }

    @Override
    public int delete(ConnectConfig connectConfig, String tableName, String condition, Map<String, Object> params) {
        return delegate.delete(connectConfig, tableName, condition, params);
    }
}
