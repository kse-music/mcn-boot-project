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
     * 获取表模式信息
     */
    List<SchemaInfo> schemaInfo(ConnectConfig connectConfig, DbQuery dbQuery);

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

    Long countData(ConnectConfig connectConfig, DataQuery dataQuery);

    RestResp<List<Map<String, Object>>> pageData(ConnectConfig connectConfig, DataQuery dataQuery);

    /**
     * 新增数据
     * @param connectConfig 连接配置
     * @param tableName 表名
     * @param data 要插入的数据
     * @return 影响行数
     */
    int insert(ConnectConfig connectConfig, String tableName, Map<String, Object> data);

    /**
     * 修改数据
     * @param connectConfig 连接配置
     * @param tableName 表名
     * @param data 要修改的字段
     * @param condition where条件
     * @param params 条件参数
     * @return 影响行数
     */
    int update(ConnectConfig connectConfig, String tableName, Map<String, Object> data,
               String condition, Map<String, Object> params);

    /**
     * 删除数据
     * @param connectConfig 连接配置
     * @param tableName 表名
     * @param condition where条件
     * @param params 条件参数
     * @return 影响行数
     */
    int delete(ConnectConfig connectConfig, String tableName, String condition, Map<String, Object> params);

    static RdbManage defaults() {
        return new DefaultRdbManage();
    }

}
