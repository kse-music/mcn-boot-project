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
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public static BeanPostProcessor dataSourceScriptDatabaseInitializerBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if(bean instanceof SqlDataSourceScriptDatabaseInitializer){
                    return new CustomSqlDataSourceScriptDatabaseInitializer(SpringBeanUtils.getBean(DataSource.class), SpringBeanUtils.getBean(SqlInitializationProperties.class));
                }
                return bean;
            }

        };
    }

    static class CustomSqlDataSourceScriptDatabaseInitializer extends SqlDataSourceScriptDatabaseInitializer{
        private static final String OPTIONAL_LOCATION_PREFIX = "optional:";

        private final SqlInitializationProperties properties;
        private final SqlInitProperties sqlInitProperties;

        public CustomSqlDataSourceScriptDatabaseInitializer(DataSource dataSource, SqlInitializationProperties properties) {
            super(dataSource,properties);
            this.properties = properties;
            this.sqlInitProperties = SpringBeanUtils.getBean(SqlInitProperties.class);
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            super.afterPropertiesSet();
            String fallback = "additional-script";
            runScripts(getScripts(scriptLocations(sqlInitProperties.getScriptLocations(), fallback),fallback), properties.isContinueOnError(), sqlInitProperties.getSeparator(),properties.getEncoding());
        }

        private List<String> scriptLocations(List<String> locations, String fallback) {
            if (locations != null) {
                return locations;
            }
            List<String> fallbackLocations = new ArrayList<>();
            fallbackLocations.add("optional:classpath*:" + fallback + "-" + properties.getPlatform() + ".sql");
            fallbackLocations.add("optional:classpath*:" + fallback + ".sql");
            return fallbackLocations;
        }

        private List<Resource> getScripts(List<String> locations, String type) {
            if (CollectionUtils.isEmpty(locations)) {
                return Collections.emptyList();
            }
            ScriptLocationResolver locationResolver = new ScriptLocationResolver(null);
            List<Resource> resources = new ArrayList<>();
            for (String location : locations) {
                boolean optional = location.startsWith(OPTIONAL_LOCATION_PREFIX);
                if (optional) {
                    location = location.substring(OPTIONAL_LOCATION_PREFIX.length());
                }
                for (Resource resource : doGetResources(location, locationResolver)) {
                    if (resource.exists()) {
                        resources.add(resource);
                    }
                    else if (!optional) {
                        throw new IllegalStateException("No " + type + " scripts found at location '" + location + "'");
                    }
                }
            }
            return resources;
        }

        private List<Resource> doGetResources(String location, ScriptLocationResolver locationResolver) {
            try {
                return locationResolver.resolve(location);
            }
            catch (Exception ex) {
                throw new IllegalStateException("Unable to load resources from " + location, ex);
            }
        }

    }

    private static class ScriptLocationResolver {

        private final ResourcePatternResolver resourcePatternResolver;

        ScriptLocationResolver(ResourceLoader resourceLoader) {
            this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        }

        private List<Resource> resolve(String location) throws IOException {
            List<Resource> resources = new ArrayList<>(
                    Arrays.asList(this.resourcePatternResolver.getResources(location)));
            resources.sort((r1, r2) -> {
                try {
                    return r1.getURL().toString().compareTo(r2.getURL().toString());
                }
                catch (IOException ex) {
                    return 0;
                }
            });
            return resources;
        }

    }
}