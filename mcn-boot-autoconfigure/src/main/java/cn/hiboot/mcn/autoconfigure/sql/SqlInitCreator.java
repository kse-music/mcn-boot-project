package cn.hiboot.mcn.autoconfigure.sql;

import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * SqlInitCreator
 *
 * @author DingHao
 * @since 2022/10/11 12:39
 */
class SqlInitCreator {
    private static final String OPTIONAL_LOCATION_PREFIX = "optional:";

    static CustomSqlDataSourceScriptDatabaseInitializer create(){
        SqlInitProperties sqlInitProperties = SpringBeanUtils.getBean(SqlInitProperties.class);
        SqlInitializationProperties properties = SpringBeanUtils.getBean(SqlInitializationProperties.class);
        CustomDatabaseInitializationSettings settings = new CustomDatabaseInitializationSettings();
        settings.setSchemaLocations(scriptLocations(properties.getSchemaLocations(),sqlInitProperties.getDir(), "schema",properties.getPlatform()));
        settings.setScriptLocations(scriptLocations(sqlInitProperties.getLocations(),sqlInitProperties.getDir(),"other",properties.getPlatform()));
        settings.setDataLocations(scriptLocations(properties.getDataLocations(),sqlInitProperties.getDir(),"data",properties.getPlatform()));
        settings.setOtherSeparator(sqlInitProperties.getSeparator());
        settings.setContinueOnError(properties.isContinueOnError());
        settings.setSeparator(properties.getSeparator());
        settings.setEncoding(properties.getEncoding());
        settings.setMode(properties.getMode());
        settings.setInitDdName(sqlInitProperties.getInitDbName());
        return new CustomSqlDataSourceScriptDatabaseInitializer(SpringBeanUtils.getBean(DataSource.class),settings);
    }

    static List<String> scriptLocations(List<String> locations,String dir, String fallback,String platform) {
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

    private static class CustomSqlDataSourceScriptDatabaseInitializer extends SqlDataSourceScriptDatabaseInitializer {
        private final CustomDatabaseInitializationSettings settings;

        public CustomSqlDataSourceScriptDatabaseInitializer(DataSource dataSource, CustomDatabaseInitializationSettings settings) {
            super(dataSource,settings);
            this.settings = settings;
        }

        @Override
        public boolean initializeDatabase() {
            ScriptLocationResolver locationResolver = new ScriptLocationResolver(SpringBeanUtils.getApplicationContext());
            createDatabase(SpringBeanUtils.getBean(DataSourceProperties.class));
            boolean initialized = applySchemaScripts(locationResolver);
            boolean other = applyOtherScripts(locationResolver);
            return applyDataScripts(locationResolver) || other || initialized;
        }

        private void createDatabase(DataSourceProperties properties){
            String initDdName = settings.getInitDdName();
            if(ObjectUtils.isEmpty(initDdName)){
                return;
            }
            try{
                String url = properties.getUrl();
                int qPos = url.indexOf(63);
                String urlServer = url;
                if (qPos != -1) {
                    urlServer = url.substring(0, qPos);
                }
                int slash = urlServer.lastIndexOf(47);
                String dbName = urlServer.substring(slash + 1);
                url = urlServer.substring(0, slash + 1) + initDdName;
                try(Connection con = DriverManager.getConnection(url,properties.getUsername(),properties.getPassword())){
                    con.prepareStatement("CREATE DATABASE ".concat(dbName)).executeUpdate();
                }
            }catch (SQLException e){
                //ignore
            }
        }

        private boolean applySchemaScripts(ScriptLocationResolver locationResolver) {
            return applyScripts(this.settings.getSchemaLocations(), "schema", locationResolver,null);
        }

        private boolean applyOtherScripts(ScriptLocationResolver locationResolver) {
            return applyScripts(this.settings.getScriptLocations(), "other", locationResolver,this.settings.getOtherSeparator());
        }

        private boolean applyDataScripts(ScriptLocationResolver locationResolver) {
            return applyScripts(this.settings.getDataLocations(), "data", locationResolver,null);
        }

        private boolean applyScripts(List<String> locations, String type, ScriptLocationResolver locationResolver,String separator) {
            List<Resource> scripts = getScripts(locations, type, locationResolver);
            if (!scripts.isEmpty() && isEnabled()) {
                runScripts(scripts,separator);
                return true;
            }
            return false;
        }

        private boolean isEnabled() {
            if (this.settings.getMode() == DatabaseInitializationMode.NEVER) {
                return false;
            }
            return this.settings.getMode() == DatabaseInitializationMode.ALWAYS || isEmbeddedDatabase();
        }

        private List<Resource> getScripts(List<String> locations, String type, ScriptLocationResolver locationResolver) {
            if (CollectionUtils.isEmpty(locations)) {
                return Collections.emptyList();
            }
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
            }catch (Exception ex) {
                throw new IllegalStateException("Unable to load resources from " + location, ex);
            }
        }

        private void runScripts(List<Resource> resources,String separator) {
            runScripts(resources, this.settings.isContinueOnError(), separator == null ? this.settings.getSeparator() : separator, this.settings.getEncoding());
        }

    }

    private static class CustomDatabaseInitializationSettings extends DatabaseInitializationSettings{
        private List<String> scriptLocations;
        private String otherSeparator = ";";
        private String initDdName;

        public List<String> getScriptLocations() {
            return scriptLocations;
        }

        public void setScriptLocations(List<String> scriptLocations) {
            this.scriptLocations = scriptLocations;
        }

        public String getOtherSeparator() {
            return otherSeparator;
        }

        public void setOtherSeparator(String otherSeparator) {
            this.otherSeparator = otherSeparator;
        }

        public String getInitDdName() {
            return initDdName;
        }

        public void setInitDdName(String initDdName) {
            this.initDdName = initDdName;
        }
    }

    private static class ScriptLocationResolver {

        private final ResourcePatternResolver resourcePatternResolver;

        ScriptLocationResolver(ResourceLoader resourceLoader) {
            this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        }

        private List<Resource> resolve(String location) throws IOException {
            List<Resource> resources = new ArrayList<>(Arrays.asList(this.resourcePatternResolver.getResources(location)));
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
