package cn.hiboot.mcn.autoconfigure.mybatis;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
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
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * MybatisQuickAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/2 0:25
 */
@AutoConfiguration
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
public class SingleDataSourceAutoConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {

    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String name = MapperScannerConfigurer.class.getName();//use default config
        if(registry.containsBeanDefinition(name)){
            BeanDefinition beanDefinition = registry.getBeanDefinition(name);
            beanDefinition.getPropertyValues().removePropertyValue("annotationClass");
            String toScanPkg = environment.getProperty(ConfigProperties.APP_BASE_PACKAGE) + ".dao";
            String additionalPkg = environment.getProperty("mapper.scan.additional-package", "");
            if (StringUtils.hasText(additionalPkg)) {
                toScanPkg += "," + additionalPkg;
            }
            beanDefinition.getPropertyValues().addPropertyValue("basePackage", toScanPkg);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
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
