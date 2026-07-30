package cn.hiboot.mcn.autoconfigure.jdbc;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Set;

/**
 * OnMissingPrimaryDataSource
 *
 * @author DingHao
 * @since 2023/9/5 15:36
 */
public class OnMissingPrimaryDataSource implements AutoConfigurationImportFilter, EnvironmentAware {

    private static final Set<String> EXCLUDED = Set.of(
            "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
            "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"
    );

    private boolean primaryDatasourceExist;

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            String candidate = autoConfigurationClasses[i];
            matches[i] = primaryDatasourceExist || candidate == null || !EXCLUDED.contains(candidate);
        }
        return matches;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.primaryDatasourceExist = Binder.get(environment).bind("spring.datasource.url", String.class).orElse(null) != null;
    }
}
