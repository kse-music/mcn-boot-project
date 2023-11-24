package cn.hiboot.mcn.autoconfigure.sql;

import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
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
@AutoConfiguration(after = SqlInitializationAutoConfiguration.class )
@EnableConfigurationProperties(SqlInitProperties.class)
@ConditionalOnBean(SqlDataSourceScriptDatabaseInitializer.class)
@ConditionalOnClass(DatabasePopulator.class)
public class SqlInitAutoConfiguration {

    @Bean
    static BeanPostProcessor dataSourceScriptDatabaseInitializerBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if(bean instanceof SqlDataSourceScriptDatabaseInitializer){
                    return create();
                }
                return bean;
            }

        };
    }

    private static CustomSqlDataSourceScriptDatabaseInitializer create(){
        SqlInitProperties sqlInitProperties = SpringBeanUtils.getBean(SqlInitProperties.class);
        SqlInitializationProperties properties = SpringBeanUtils.getBean(SqlInitializationProperties.class);
        CustomDatabaseInitializationSettings settings = new CustomDatabaseInitializationSettings();

        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        propertyMapper.from(scriptLocations(properties.getSchemaLocations(),sqlInitProperties.getDir(), "schema",properties.getPlatform())).to(settings::setSchemaLocations);
        propertyMapper.from(scriptLocations(sqlInitProperties.getLocations(),sqlInitProperties.getDir(),"other",properties.getPlatform())).to(settings::setScriptLocations);
        propertyMapper.from(scriptLocations(properties.getDataLocations(),sqlInitProperties.getDir(),"data",properties.getPlatform())).to(settings::setDataLocations);
        propertyMapper.from(sqlInitProperties::getSeparator).to(settings::setOtherSeparator);
        propertyMapper.from(sqlInitProperties::getInitDbName).to(settings::setInitDdName);
        propertyMapper.from(properties::isContinueOnError).to(settings::setContinueOnError);
        propertyMapper.from(properties::getSeparator).to(settings::setSeparator);
        propertyMapper.from(properties::getEncoding).to(settings::setEncoding);
        propertyMapper.from(properties::getMode).to(settings::setMode);
        propertyMapper.from(properties::getPlatform).to(settings::setPlatform);

        return new CustomSqlDataSourceScriptDatabaseInitializer(SpringBeanUtils.getBean(DataSource.class),settings);
    }

    private static List<String> scriptLocations(List<String> locations, String dir, String fallback, String platform) {
        if (locations != null) {
            return locations;
        }
        if (StringUtils.hasLength(dir)) {
            fallback = dir + "/" + fallback;
        }
        List<String> fallbackLocations = new ArrayList<>();
        fallbackLocations.add("optional:classpath*:" + fallback + "-" + platform + ".sql");
        fallbackLocations.add("optional:classpath*:" + fallback + ".sql");
        return fallbackLocations;
    }

}