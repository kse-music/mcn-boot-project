package cn.hiboot.mcn.autoconfigure.jdbc.manage;

/**
 * DelegateDbTypeProvider
 *
 * @author DingHao
 * @since 2025/11/12 11:08
 */
public class DelegateDbTypeProvider implements DbTypeProvider {

    private final DbTypeProvider delegate;

    public DelegateDbTypeProvider(DbTypeProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public String driverClassName() {
        return delegate.driverClassName();
    }

    @Override
    public String url(ConnectConfig connectConfig) {
        return delegate.url(connectConfig);
    }

    @Override
    public String pageSql(String sql, Integer skip, Integer limit) {
        return delegate.pageSql(sql, skip, limit);
    }

    @Override
    public String sqlQuote(String str) {
        return delegate.sqlQuote(str);
    }

}
