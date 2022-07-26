package cn.hiboot.mcn.autoconfigure.jpa;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * JpaConfiguration
 *
 * @author DingHao
 * @since 2022/7/26 23:13
 */
class JpaConfiguration {
    private DataSource dataSource;
    private String packages;
    private String persistenceUnit;
    private JpaProperties properties;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public void setPersistenceUnit(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }

    public void setJpaProperties(JpaProperties jpaProperties) {
        this.properties = jpaProperties;
    }

    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(EntityManagerFactoryBuilder builder) {
        return builder.dataSource(dataSource)
                .packages(packages)
                .properties(properties.getProperties())
                .persistenceUnit(persistenceUnit)
                .build();
    }

    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean) {
        return new JpaTransactionManager(localContainerEntityManagerFactoryBean.getObject());
    }

    public JpaVendorAdapter jpaVendorAdapter() {
        AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(this.properties.isShowSql());
        if (this.properties.getDatabase() != null) {
            adapter.setDatabase(this.properties.getDatabase());
        }
        if (this.properties.getDatabasePlatform() != null) {
            adapter.setDatabasePlatform(this.properties.getDatabasePlatform());
        }
        adapter.setGenerateDdl(this.properties.isGenerateDdl());
        return adapter;
    }

    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(JpaVendorAdapter jpaVendorAdapter,
                                                                   ObjectProvider<PersistenceUnitManager> persistenceUnitManager,
                                                                   ObjectProvider<EntityManagerFactoryBuilderCustomizer> customizers) {
        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(jpaVendorAdapter,
                this.properties.getProperties(), persistenceUnitManager.getIfAvailable());
        customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return builder;
    }

}
