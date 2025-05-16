package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.model.result.RestResp;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * RdbManage
 *
 * @author DingHao
 * @since 2025/5/15 15:01
 */
public interface RdbManage {

    void connect(ConnectConfig connectConfig, Consumer<Connection> consumer);

    default String testConnect(ConnectConfig connectConfig) {
        String[] result = new String[1];
        connect(connectConfig, connection -> result[0] = connection == null ? "fail" : "success");
        return result[0];
    }

    /*
     * 获取表信息
     */
    List<TableInfo> tableInfo(ConnectConfig connectConfig, DbQuery dbQuery);

    /*
     * 获取字段信息
     */
    List<FieldInfo> findFieldInfo(ConnectConfig connectConfig, DbQuery dbQuery);

    /*
     * 获取外键信息
     */
    List<ImportedKeyInfo> findImportedKeys(ConnectConfig connectConfig, DbQuery dbQuery);

    /*
     * 分页读取数据
     */
    List<Map<String, Object>> listData(ConnectConfig connectConfig, DataQuery dataQuery);

    /*
     * count
     */
    Long countData(ConnectConfig connectConfig, DataQuery dataQuery);

    RestResp<List<Map<String, Object>>> pageData(ConnectConfig connectConfig, DataQuery dataQuery);

    static RdbManage defaults() {
        return new DefaultRdbManage();
    }

}
