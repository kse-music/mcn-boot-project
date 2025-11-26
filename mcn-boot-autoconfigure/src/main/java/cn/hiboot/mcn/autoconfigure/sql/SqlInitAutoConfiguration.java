package cn.hiboot.mcn.autoconfigure.sql;

import cn.hiboot.mcn.autoconfigure.minio.MinioAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.jdbc.autoconfigure.DataSourceInitializationAutoConfiguration;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.autoconfigure.init.SqlInitializationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * SqlInitAutoConfiguration
 *
 * @author DingHao
 * @since 2022/10/10 14:17
 */
@AutoConfiguration(after = {DataSourceInitializationAutoConfiguration.class, MinioAutoConfiguration.class})
@EnableConfigurationProperties(SqlInitProperties.class)
@ConditionalOnProperty(prefix = "spring.sql.init.additional", name = "enabled", matchIfMissing = true)
@ConditionalOnBean(DataSourceScriptDatabaseInitializer.class)
public class SqlInitAutoConfiguration {

    private final SqlInitProperties sqlInitProperties;

    public SqlInitAutoConfiguration(SqlInitProperties sqlInitProperties) {
        this.sqlInitProperties = sqlInitProperties;
    }

    @Bean
    DataSourceScriptDatabaseInitializer mcnDataSourceScriptDatabaseInitializer(DataSource dataSource, SqlInitializationProperties properties) {
        CustomDatabaseInitializationSettings settings = new CustomDatabaseInitializationSettings();
        PropertyMapper propertyMapper = PropertyMapper.get();
        propertyMapper.from(scriptLocations("schema", properties.getPlatform())).to(settings::setSchemaLocations);
        propertyMapper.from(scriptLocations("other", properties.getPlatform())).to(settings::setScriptLocations);
        propertyMapper.from(scriptLocations("data", properties.getPlatform())).to(settings::setDataLocations);
        propertyMapper.from(sqlInitProperties::getSeparator).to(settings::setOtherSeparator);
        propertyMapper.from(sqlInitProperties::getInitDbName).to(settings::setInitDdName);
        propertyMapper.from(properties::isContinueOnError).to(settings::setContinueOnError);
        propertyMapper.from(properties::getSeparator).to(settings::setSeparator);
        propertyMapper.from(properties::getEncoding).to(settings::setEncoding);
        propertyMapper.from(properties::getMode).to(settings::setMode);
        propertyMapper.from(properties::getPlatform).to(settings::setPlatform);
        return new CustomSqlDataSourceScriptDatabaseInitializer(dataSource, settings);
    }

    private List<String> scriptLocations(String fallback, String platform) {
        if (StringUtils.hasLength(sqlInitProperties.getDir())) {
            fallback = sqlInitProperties.getDir() + "/" + fallback;
        }
        return buildScriptLocations(fallback, platform);
    }

    static List<String> buildScriptLocations(String fallback, String platform) {
        List<String> fallbackLocations = new ArrayList<>(2);
        fallbackLocations.add("optional:classpath*:" + fallback + "-" + platform + ".sql");
        fallbackLocations.add("optional:classpath*:" + fallback + ".sql");
        return fallbackLocations;
    }

}