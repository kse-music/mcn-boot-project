package cn.hiboot.mcn.autoconfigure.db;

import cn.hiboot.mcn.autoconfigure.context.McnPropertiesPostProcessor;
import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.HashMap;
import java.util.Map;

public class MultipleDataSourceRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private Environment environment;

    public MultipleDataSourceRegistryPostProcessor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
            String[] dbs = env.getProperty(MultipleMybatisAutoConfiguration.PREFIX + "name", String[].class);
            String basePackage = env.getProperty((McnPropertiesPostProcessor.APP_BASE_PACKAGE));
            for (String db : dbs) {
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(HikariDataSource.class);
                beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                beanDefinition.setSynthetic(true);
//                configConnectPool(beanDefinition, db);
                StringBuilder sb = new StringBuilder();
                registry.registerBeanDefinition(sb.append(db).append("DataSource").toString(), beanDefinition);

                try {
                    SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
                    factoryBean.setVfs(SpringBootVFS.class);
                    ResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
                    org.apache.ibatis.session.Configuration conf = new org.apache.ibatis.session.Configuration();
                    conf.setMapUnderscoreToCamelCase(true);

                    RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(SqlSessionFactoryBean.class);
                    rootBeanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                    Map<String, Object> map = new HashMap<>();
                    map.put("vfs", SpringBootVFS.class);
                    map.put("dataSource", new RuntimeBeanReference(sb.toString()));

                    sb.setLength(0);
                    sb.append("classpath*:mapper/").append(db).append("/*.xml");
                    map.put("mapperLocations", pathResolver.getResources(sb.toString()));

                    sb.setLength(0);
                    sb.append(basePackage).append(".bean");
                    map.put("typeAliasesPackage", sb.toString());

                    sb.setLength(0);
                    sb.append(basePackage).append(".dao.handler");
                    map.put("typeHandlersPackage", sb.toString());

                    map.put("configuration", conf);
                    rootBeanDefinition.getPropertyValues().addPropertyValues(map);

                    sb.setLength(0);
                    sb.append(db).append("SqlSessionFactory");
                    registry.registerBeanDefinition(sb.toString(), rootBeanDefinition);

                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }


}
