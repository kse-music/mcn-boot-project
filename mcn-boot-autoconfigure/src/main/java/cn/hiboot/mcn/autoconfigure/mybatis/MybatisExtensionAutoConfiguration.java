package cn.hiboot.mcn.autoconfigure.mybatis;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.autoconfigure.jdbc.MultipleDataSourceConfig;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * MybatisQuickAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/2 0:25
 */
@AutoConfiguration
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
public class MybatisExtensionAutoConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {

    private static final String STANDARD_SQL_SESSION_FACTORY_BEAN_NAME = "sqlSessionFactory";
    private static final String STANDARD_SQL_SESSION_TEMPLATE_BEAN_NAME = "sqlSessionTemplate";

    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String name = MapperScannerConfigurer.class.getName();
        if(registry.containsBeanDefinition(name)){//The base package is scanned by default without the MapperScan annotation,
            BeanDefinition beanDefinition = registry.getBeanDefinition(name);
            beanDefinition.getPropertyValues().removePropertyValue("annotationClass");
            String daoPackageName = environment.getProperty(ConfigProperties.DAO_PACKAGE_NAME, "dao");
            String toScanPkg = environment.getProperty(ConfigProperties.APP_BASE_PACKAGE) + "." + daoPackageName;//modify scan base package + .dao
            String additionalPkg = environment.getProperty("mapper.scan.additional-package", "");
            if (StringUtils.hasText(additionalPkg)) {
                toScanPkg += "," + additionalPkg;
            }
            beanDefinition.getPropertyValues().addPropertyValue("basePackage", toScanPkg);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        MultipleDataSourceConfig multipleDataSourceConfig = beanFactory.getBean(MultipleDataSourceConfig.class);
        Map<String, DataSourceProperties> properties = multipleDataSourceConfig.getProperties();
        if(properties.isEmpty()){
            return;
        }
        if(beanFactory.containsBean(STANDARD_SQL_SESSION_FACTORY_BEAN_NAME)){
            beanFactory.getBeanDefinition(STANDARD_SQL_SESSION_FACTORY_BEAN_NAME).setPrimary(true);
        }
        if(beanFactory.containsBean(STANDARD_SQL_SESSION_TEMPLATE_BEAN_NAME)){
            beanFactory.getBeanDefinition(STANDARD_SQL_SESSION_TEMPLATE_BEAN_NAME).setPrimary(true);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
            return 0;
        }


}
