package cn.hiboot.mcn.autoconfigure.sql;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceSchemaCreatedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.log.LogMessage;

import javax.sql.DataSource;

/**
 * DataSourceInitializerInvoker
 *
 * @author DingHao
 * @since 2022/12/17 21:58
 */
class DataSourceInitializerInvoker implements ApplicationListener<DataSourceSchemaCreatedEvent>, InitializingBean {

    private static final Log logger = LogFactory.getLog(DataSourceInitializerInvoker.class);

    private final ObjectProvider<DataSource> dataSource;

    private final DataSourceProperties properties;
    private final SqlInitProperties sqlInitProperties;

    private final ApplicationContext applicationContext;

    private DataSourceInitializer dataSourceInitializer;

    private boolean initialized;

    DataSourceInitializerInvoker(ObjectProvider<DataSource> dataSource, DataSourceProperties properties, SqlInitProperties sqlInitProperties,
                                 ApplicationContext applicationContext) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.sqlInitProperties = sqlInitProperties;
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        DataSourceInitializer initializer = getDataSourceInitializer();
        if (initializer != null) {
            boolean schemaCreated = this.dataSourceInitializer.createSchema();
            if (schemaCreated) {
                initialize(initializer);
            }
        }
    }

    private void initialize(DataSourceInitializer initializer) {
        try {
            this.applicationContext.publishEvent(new DataSourceSchemaCreatedEvent(initializer.getDataSource()));
            if (!this.initialized) {
                this.dataSourceInitializer.initSchema();
                this.initialized = true;
            }
        }
        catch (IllegalStateException ex) {
            logger.warn(LogMessage.format("Could not send event to complete DataSource initialization (%s)", ex.getMessage()));
        }
    }

    @Override
    public void onApplicationEvent(DataSourceSchemaCreatedEvent event) {
        // NOTE the event can happen more than once and
        // the event datasource is not used here
        DataSourceInitializer initializer = getDataSourceInitializer();
        if (!this.initialized && initializer != null) {
            initializer.initSchema();
            this.initialized = true;
        }
    }

    private DataSourceInitializer getDataSourceInitializer() {
        if (this.dataSourceInitializer == null) {
            DataSource ds = this.dataSource.getIfUnique();
            if (ds != null) {
                this.dataSourceInitializer = new DataSourceInitializer(ds, this.properties,this.sqlInitProperties, this.applicationContext);
            }
        }
        return this.dataSourceInitializer;
    }

}