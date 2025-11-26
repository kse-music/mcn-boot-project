package cn.hiboot.mcn.autoconfigure.jdbc;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * OnMissingPrimaryDataSource
 *
 * @author DingHao
 * @since 2023/9/5 15:36
 */
public class OnMissingPrimaryDataSource implements AutoConfigurationImportFilter, EnvironmentAware {

    private boolean primaryDatasourceExist;

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] rs = new boolean[autoConfigurationClasses.length];
        Arrays.fill(rs,true);
        if(!primaryDatasourceExist){
            for (int i = 0; i < autoConfigurationClasses.length; i++) {
                if(DataJpaRepositoriesAutoConfiguration.class.getName().equals(autoConfigurationClasses[i])
                        || HibernateJpaAutoConfiguration.class.getName().equals(autoConfigurationClasses[i])
                        || DataSourceAutoConfiguration.class.getName().equals(autoConfigurationClasses[i])){
                    rs[i] = false;
                }
            }
        }
        return rs;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.primaryDatasourceExist = Binder.get(environment).bind("spring.datasource.url", String.class).orElse(null) != null;
    }
}
