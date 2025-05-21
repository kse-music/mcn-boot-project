package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.model.base.FieldSort;
import cn.hiboot.mcn.core.util.McnUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RdbManageUtil
 *
 * @author DingHao
 * @since 2025/5/16 14:16
 */
abstract class RdbManageUtil {

    private static final ThreadLocal<SimpleDateFormat> sdfWt = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    private static final ThreadLocal<SimpleDateFormat> sdfNt = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    void printMetaData(ResultSet rs) throws SQLException {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int columnCount = rsMetaData.getColumnCount();
        while (rs.next()) {
            StringBuilder row = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rsMetaData.getColumnName(i);
                Object value = rs.getObject(i);
                row.append(columnName).append(":").append(value);
                if (i < columnCount) {
                    row.append(", ");
                }
            }
            System.out.println(row);
        }
    }

    static String buildCondition(ConnectConfig connectConfig, DataQuery dataQuery, Map<String, Object> paramMap) {
        List<FieldQuery> query = dataQuery.getQuery();
        if (McnUtils.isNullOrEmpty(query)) {
            return "";
        }
        String sqlQuote = sqlQuote(connectConfig.getDbType());
        List<String> conditions = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);
        for (FieldQuery fq : query) {
            String column = fq.getName();
            String quotedColumn = fieldName(column, sqlQuote);
            Object value = fq.getValue();
            String operator = fq.getOperator().valueString();
            String paramKey = column + "_" + index.getAndIncrement();
            switch (operator.toLowerCase()) {
                case "like":
                    conditions.add(quotedColumn + " LIKE :" + paramKey);
                    paramMap.put(paramKey, "%" + value + "%");
                    break;
                case "in":
                    conditions.add(quotedColumn + " IN (:" + paramKey + ")");
                    paramMap.put(paramKey, value);
                    break;
                default:
                    conditions.add(quotedColumn + " " + operator + " :" + paramKey);
                    paramMap.put(paramKey, value);
            }
        }
        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    private static String sqlQuote(String dbType) {
        if (dbType.equals("mysql")) {
            return "`";
        }
        return "\"";
    }

    private static String fieldName(String columnName, String sqlQuote) {
        return sqlQuote + columnName + sqlQuote;
    }

    static String buildSort(DataQuery dataQuery) {
        List<FieldQuery> query = dataQuery.getQuery();
        if (McnUtils.isNullOrEmpty(query)) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        List<FieldSort> order = dataQuery.getSort();
        for (int i = 0; i < order.size(); i++) {
            if (i == 0) {
                sql.append(" ORDER BY ");
            } else {
                sql.append(" , ");
            }
            FieldSort fieldSort = order.get(i);
            sql.append(fieldSort.getField()).append(" ").append(fieldSort.getSort());
        }
        return sql.toString();
    }

    static Map<String, Object> tranMap(Map<String, Object> map) {
        map.replaceAll((key, value) -> {
            if (value instanceof java.sql.Date) {
                return sdfNt.get().format((Date) value);
            } else if (value instanceof Timestamp) {
                return sdfWt.get().format((Date) value);
            } else if (value instanceof LocalDateTime) {
                return McnUtils.localDateTimeToString((LocalDateTime) value);
            } else if (value instanceof LocalDate) {
                return McnUtils.localDateToDate((LocalDate) value);
            }
            return value;
        });
        return map;
    }

}
