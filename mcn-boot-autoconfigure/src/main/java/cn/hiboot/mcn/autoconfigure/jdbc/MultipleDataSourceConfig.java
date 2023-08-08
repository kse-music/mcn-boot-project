package cn.hiboot.mcn.autoconfigure.jdbc;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * MultipleDataSourceConfig
 *
 * @author DingHao
 * @since 2022/7/28 17:41
 */
public class MultipleDataSourceConfig {
    private final Map<String, DataSourceProperties> properties;
    private final Environment environment;
    private final int vote;
    private final String daoPackageName;
    private String basePackage;

    public MultipleDataSourceConfig(Map<String, DataSourceProperties> properties, Environment environment){
        this.properties = properties;
        this.environment = environment;
        this.vote = checkConfig(environment);
        this.daoPackageName = environment.getProperty(ConfigProperties.DAO_PACKAGE_NAME, "dao");
        this.basePackage = environment.getProperty(ConfigProperties.BASE_PACKAGE_NAME);
        if(McnUtils.isNullOrEmpty(basePackage)){
            this.basePackage = environment.getProperty(ConfigProperties.APP_BASE_PACKAGE);
        }
    }

    private int checkConfig(Environment environment) {
        int vote = 0;
        if(environment.getProperty(ConfigProperties.JPA_MULTIPLE_DATASOURCE_PREFIX+".enable",Boolean.class,false)){
            vote++;
        }
        if(environment.getProperty(ConfigProperties.MYBATIS_MULTIPLE_DATASOURCE_PREFIX+".enable",Boolean.class,false)){
            vote++;
        }
        if(environment.getProperty(ConfigProperties.DYNAMIC_DATASOURCE_PREFIX+".enable",Boolean.class,false)){
            vote++;
        }
        if(vote > 1){
            throw new IllegalArgumentException("mybatis and jpa multiple datasource and dynamic datasource only config one!");
        }
        return vote;
    }

    public Map<String, DataSourceProperties> getProperties() {
        return properties;
    }

    public String getDaoPackageName() {
        return daoPackageName;
    }

    public String getBasePackage() {
        return basePackage;
    }

    boolean enableDynamicDatasource(){
        Boolean enable = environment.getProperty(ConfigProperties.DYNAMIC_DATASOURCE_PREFIX + ".enable", Boolean.class);
        if(vote == 0){//未启用jpa和mybatis以及DynamicDatasource
            return enable == null || enable;
        }
        return Boolean.TRUE.equals(enable);
    }

}
