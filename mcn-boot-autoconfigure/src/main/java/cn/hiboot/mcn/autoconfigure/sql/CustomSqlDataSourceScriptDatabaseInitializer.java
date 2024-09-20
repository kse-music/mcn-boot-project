package cn.hiboot.mcn.autoconfigure.sql;

import cn.hiboot.mcn.autoconfigure.minio.FileUploadInfoCache;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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
 * CustomSqlDataSourceScriptDatabaseInitializer
 *
 * @author DingHao
 * @since 2023/11/15 16:29
 */
class CustomSqlDataSourceScriptDatabaseInitializer extends DataSourceScriptDatabaseInitializer {

    private static final String OPTIONAL_LOCATION_PREFIX = "optional:";

    private final CustomDatabaseInitializationSettings settings;

    public CustomSqlDataSourceScriptDatabaseInitializer(DataSource dataSource, CustomDatabaseInitializationSettings settings) {
        super(dataSource,settings);
        this.settings = settings;
    }

    @Override
    public boolean initializeDatabase() {
        ApplicationContext applicationContext = SpringBeanUtils.getApplicationContext();
        for (String s : applicationContext.getBeanNamesForType(FileUploadInfoCache.class)) {
            if (FileUploadInfoCache.class == applicationContext.getType(s)) {
                settings.getSchemaLocations().addAll(SqlInitAutoConfiguration.buildScriptLocations("cn/hiboot/mcn/autoconfigure/minio/minio", settings.getPlatform()));
                break;
            }
        }
        ScriptLocationResolver locationResolver = new ScriptLocationResolver(applicationContext);
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
                con.prepareStatement(DatabaseDriver.createDatabase(settings.getPlatform(),dbName)).executeUpdate();
            }
        }catch (SQLException ignored){
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

    private boolean applyScripts(List<String> locations, String type, ScriptLocationResolver locationResolver, String separator) {
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
        runScripts(new Scripts(resources).continueOnError(this.settings.isContinueOnError())
                .separator(separator == null ? this.settings.getSeparator() : separator)
                .encoding(this.settings.getEncoding()));
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
