package cn.hiboot.mcn.autoconfigure.jdbc;

/**
 * DataSourceHolder
 *
 * @author DingHao
 * @since 2022/7/28 16:25
 */
public class DataSourceHolder {
    private static final ThreadLocal<String> dataSources = new InheritableThreadLocal<>();

    public static void setDataSource(String datasource) {
        dataSources.set(datasource);
    }

    public static String getDataSource() {
        return dataSources.get();
    }

    public static void clearDataSource() {
        dataSources.remove();
    }
}
