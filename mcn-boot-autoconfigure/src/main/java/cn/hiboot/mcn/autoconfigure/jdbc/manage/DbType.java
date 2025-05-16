package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import org.springframework.boot.jdbc.DatabaseDriver;

/**
 * DbType
 *
 * @author DingHao
 * @since 2025/5/15 14:11
 */
enum DbType {

    dm("dm.jdbc.driver.DmDriver"),
    kingbase8("com.kingbase8.Driver");

    private final String driverClassName;

    DbType(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public static String fromTable(String dbType, String schema, String table) {
        try {
            valueOf(dbType);
            return schema + "." + table;
        } catch (Exception ignored) {

        }
        return table;
    }

    public static String driverClassName(String type) {
        try {
            return valueOf(type).driverClassName;
        } catch (Exception e) {
            for (DatabaseDriver value : DatabaseDriver.values()) {
                if (value.name().equalsIgnoreCase(type)) {
                    return value.getDriverClassName();
                }
            }
            throw new IllegalArgumentException(type + " is not supported");
        }
    }

}
