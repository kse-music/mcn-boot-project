package cn.hiboot.mcn.autoconfigure.sql;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.DataSourceInitializationMode;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.config.SortedResourcesFactoryBean;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DataSourceInitializer
 *
 * @author DingHao
 * @since 2022/12/17 21:59
 */
class DataSourceInitializer {

    private static final Log logger = LogFactory.getLog(DataSourceInitializer.class);

    private final DataSource dataSource;
    private final DataSourceProperties properties;
    private final SqlInitProperties sqlInitProperties;
    private final ResourceLoader resourceLoader;

    DataSourceInitializer(DataSource dataSource, DataSourceProperties properties,SqlInitProperties sqlInitProperties, ResourceLoader resourceLoader) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.sqlInitProperties = sqlInitProperties;
        this.resourceLoader = (resourceLoader != null) ? resourceLoader : new DefaultResourceLoader(null);
    }


    DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * Create the schema if necessary.
     * @return {@code true} if the schema was created
     * @see DataSourceProperties#getSchema()
     */
    boolean createSchema() {
        createDatabase(properties);
        List<Resource> scripts = getScripts("spring.datasource.schema", this.properties.getSchema(), "schema");
        if (!scripts.isEmpty()) {
            if (!isEnabled()) {
                logger.debug("Initialization disabled (not running DDL scripts)");
                return false;
            }
            String username = this.properties.getSchemaUsername();
            String password = this.properties.getSchemaPassword();
            runScripts(scripts, this.properties.getSeparator(),username, password);
        }
        return !scripts.isEmpty();
    }

    private void createDatabase(DataSourceProperties properties){
        String initDdName = sqlInitProperties.getInitDbName();
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

    /**
     * Initialize the schema if necessary.
     * @see DataSourceProperties#getData()
     */
    void initSchema() {
        initOtherScript();
        List<Resource> scripts = getScripts("spring.datasource.data", this.properties.getData(), "data");
        if (!scripts.isEmpty()) {
            if (isEnabled()) {
                String username = this.properties.getDataUsername();
                String password = this.properties.getDataPassword();
                runScripts(scripts, this.properties.getSeparator(),username, password);
            }
        }
    }

    void initOtherScript() {
        List<Resource> scripts = getScripts("spring.sql.init.additional", this.sqlInitProperties.getLocations(), "other");
        if(scripts.isEmpty()){
            return;
        }
        if (isEnabled()) {
            String username = this.properties.getDataUsername();
            String password = this.properties.getDataPassword();
            runScripts(scripts, sqlInitProperties.getSeparator(),username, password);
        }
    }

    private boolean isEnabled() {
        DataSourceInitializationMode mode = this.properties.getInitializationMode();
        if (mode == DataSourceInitializationMode.NEVER) {
            return false;
        }
        return mode != DataSourceInitializationMode.EMBEDDED || isEmbedded();
    }

    private boolean isEmbedded() {
        try {
            return EmbeddedDatabaseConnection.isEmbedded(this.dataSource);
        }
        catch (Exception ex) {
            logger.debug("Could not determine if datasource is embedded", ex);
            return false;
        }
    }

    private List<Resource> getScripts(String propertyName, List<String> resources, String fallback) {
        if (resources != null) {
            return getResources(propertyName, resources, true);
        }
        if (StringUtils.hasLength(sqlInitProperties.getDir())) {
            fallback = sqlInitProperties.getDir() + "/" + fallback;
        }
        String platform = this.properties.getPlatform();
        List<String> fallbackResources = new ArrayList<>();
        fallbackResources.add("classpath*:" + fallback + "-" + platform + ".sql");
        fallbackResources.add("classpath*:" + fallback + ".sql");
        return getResources(propertyName, fallbackResources, false);
    }

    private List<Resource> getResources(String propertyName, List<String> locations, boolean validate) {
        List<Resource> resources = new ArrayList<>();
        for (String location : locations) {
            for (Resource resource : doGetResources(location)) {
                if (resource.exists()) {
                    resources.add(resource);
                }
                else if (validate) {
                    throw new InvalidConfigurationPropertyValueException(propertyName, resource, "No resources were found at location '" + location + "'.");
                }
            }
        }
        return resources;
    }

    private Resource[] doGetResources(String location) {
        try {
            SortedResourcesFactoryBean factory = new SortedResourcesFactoryBean(this.resourceLoader, Collections.singletonList(location));
            factory.afterPropertiesSet();
            return factory.getObject();
        }
        catch (Exception ex) {
            throw new IllegalStateException("Unable to load resources from " + location, ex);
        }
    }

    private void runScripts(List<Resource> resources, String separator,String username, String password) {
        if (resources.isEmpty()) {
            return;
        }
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(this.properties.isContinueOnError());
        populator.setSeparator(separator);
        if (this.properties.getSqlScriptEncoding() != null) {
            populator.setSqlScriptEncoding(this.properties.getSqlScriptEncoding().name());
        }
        for (Resource resource : resources) {
            populator.addScript(resource);
        }
        DataSource dataSource = this.dataSource;
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            dataSource = DataSourceBuilder.create(this.properties.getClassLoader())
                    .driverClassName(this.properties.determineDriverClassName()).url(this.properties.determineUrl())
                    .username(username).password(password).build();
        }
        DatabasePopulatorUtils.execute(populator, dataSource);
    }

}