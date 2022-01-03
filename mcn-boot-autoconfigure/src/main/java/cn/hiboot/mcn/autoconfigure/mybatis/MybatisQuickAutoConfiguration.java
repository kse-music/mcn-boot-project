package cn.hiboot.mcn.autoconfigure.mybatis;

import cn.hiboot.mcn.core.config.McnConstant;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * MybatisQuickAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/2 0:25
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SqlSessionFactory.class, MapperScan.class})
@ConditionalOnMissingBean(MapperFactoryBean.class)
@Import(MultipleDataSourceConfiguration.class)
public class MybatisQuickAutoConfiguration {

    static final String MULTIPLY_DATASOURCE_CONFIG_KEY = "multiply.datasource.name";

    @Conditional(NoMultipleDataSourceCondition.class)
    @Configuration(proxyBeanMethods = false)
    private static class AutoConfiguredMapperScannerRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {

        private Environment environment;

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            if(!registry.containsBeanDefinition(MapperScannerConfigurer.class.getName())){
                return;
            }
            BeanDefinition beanDefinition = registry.getBeanDefinition(MapperScannerConfigurer.class.getName());
            beanDefinition.getPropertyValues().removePropertyValue("annotationClass");
            String toScanPkg = environment.getProperty(McnConstant.APP_BASE_PACKAGE) + ".dao";
            String additionalPkg = environment.getProperty("mapper.scan.additional-package", "");
            if (StringUtils.hasText(additionalPkg)) {
                toScanPkg += "," + additionalPkg;
            }
            beanDefinition.getPropertyValues().addPropertyValue("basePackage", toScanPkg);
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

}
