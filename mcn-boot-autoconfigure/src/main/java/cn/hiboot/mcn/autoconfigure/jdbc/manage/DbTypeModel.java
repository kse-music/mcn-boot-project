package cn.hiboot.mcn.autoconfigure.jdbc.manage;

/**
 * DbTypeModel
 *
 * @author DingHao
 * @since 2025/11/11 9:57
 */
public class DbTypeModel implements DbTypeProvider {

    private final String name;
    private final String driverClassName;
    private final String platform;

    public DbTypeModel(String name, String driverClassName) {
        this(name,driverClassName, null);
    }

    public DbTypeModel(String name, String driverClassName, String platform) {
        this.name = name;
        this.driverClassName = driverClassName;
        this.platform = platform;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String driverClassName() {
        return this.driverClassName;
    }

    @Override
    public String platform() {
        return this.platform;
    }

}