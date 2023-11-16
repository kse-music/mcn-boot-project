package cn.hiboot.mcn.autoconfigure.sql;

/**
 * DatabaseDriver
 *
 * @author DingHao
 * @since 2023/11/15 16:04
 */
enum DatabaseDriver {

    dm("SCHEMA");

    private final String dbSql;

    DatabaseDriver(String dbSql) {
        this.dbSql = dbSql;
    }

    public static String createDatabase(String platform,String db){
        String prefix = "CREATE";
        String dbSql = "DATABASE";
        for (DatabaseDriver value : DatabaseDriver.values()) {
            if(value.name().equals(platform)){
                dbSql = value.dbSql;
                break;
            }
        }
        return prefix + " " + dbSql + " " + db;
    }

}
