package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.model.base.FieldSort;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    static String buildQuery(ConnectConfig connectConfig, DataQuery dataQuery, List<FieldInfo> fieldInfo) {
        List<FieldQuery> query = dataQuery.getQuery();
        if (McnUtils.isNullOrEmpty(query)) {
            return "";
        }
        String sqlQuote = sqlQuote(connectConfig.getDbType());
        Map<String, FieldInfo> fieldInfoMap = fieldInfo.stream().collect(Collectors.toMap(FieldInfo::getColumnName, Function.identity()));
        return query.stream()
                .map(fq -> {
                    String name = fq.getName();
                    Object value = RdbManageUtil.parseValue(fieldInfoMap.get(name).getType(), fq.getValue());
                    String operator = fq.getOperator().valueString();
                    if ("like".equals(operator)) {
                        value = "'%" + value + "%'";
                    } else if ("in".equals(operator)) {
                        value = "(" + StringUtils.collectionToDelimitedString((List<?>) value, ",", "'", "'") + ")";
                    } else if (value instanceof String) {
                        value = "'" + value + "'";
                    }
                    return fieldName(name, sqlQuote) + " " + operator + " " + value;
                })
            .collect(Collectors.collectingAndThen(Collectors.joining(" and "), s -> s.isEmpty() ? "" : " where " + s));
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
                sql.append(" order by ");
            } else {
                sql.append(" , ");
            }
            FieldSort fieldSort = order.get(i);
            sql.append(fieldSort.getField()).append(" ").append(fieldSort.getSort());
        }
        return sql.toString();
    }


    static void tranMap(Map<String, Object> map) {
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
    }

    static FieldTypeEnum convertDataType(Integer type) {
        switch (type) {
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.TINYINT:
                return FieldTypeEnum.LONG;
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.DECIMAL:
            case Types.REAL:
            case Types.NUMERIC:
                return FieldTypeEnum.FLOAT;
            case Types.DATE:
                return FieldTypeEnum.DATE;
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return FieldTypeEnum.DATA_TIME;
            default:
                return FieldTypeEnum.SHORT_TEXT;
        }
    }

    static Object parseValue(int dataType, Object value) {
        if (value == null) {
            return null;
        }
        String valueStr;
        if (value instanceof Date){
            valueStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
        } else {
            valueStr = value.toString();
        }
        if (dataType == 2) {
            valueStr = valueStr.replaceAll("E", "").replaceAll("e", "");
        } else if (dataType == 4 || dataType == 41) {
            valueStr = valueStr.replaceAll("/", "-");
            if (dataType == 4 && valueStr.length() >= 19) {
                valueStr = valueStr.substring(0, 19);
            } else if (dataType == 41 && valueStr.length() >= 10) {
                valueStr = valueStr.substring(0, 10);
            }
        }
        switch (dataType) {
            case 1:
            case 3:
                return intCheck(valueStr) ? Long.parseLong(valueStr) : null;
            case 2:
                return doubleCheck(valueStr) ? Double.parseDouble(valueStr) : null;
            case 4:
            case 41:
            case 42:
                return dateCheck(valueStr) ? valueStr : null;
            default:
                return valueStr;
        }
    }

    private static boolean intCheck(String str) {
        return str != null && Pattern.matches("[\\d]*$", str);
    }

    private static boolean doubleCheck(String str) {
        return str != null && Pattern.matches("(\\d+)(\\.\\d+)?$", str);
    }

    private static boolean dateCheck(String str) {
        return str != null && Pattern.matches("^\\d{4}[-]\\d{1,2}[-]\\d{1,2}[ ]{0,1}\\d{0,2}[:]{0,1}\\d{0,2}[:]{0,1}\\d{0,2}[:]{0,1}\\d{0,3}$", str);
    }

}
