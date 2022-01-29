package cn.hiboot.mcn.autoconfigure.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 *
 * McnBeanFactoryRegistryPostProcessor
 * 修改IOC容器即添加AutowiredAnnotationBeanPostProcessor以处理McnAutowired注解
 *
 * @author DingHao
 * @since 2019/1/7 2:09
 */
public class McnBeanFactoryRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final ConfigurableEnvironment environment;

    public McnBeanFactoryRegistryPostProcessor(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.addBeanPostProcessor(new McnBeanPostProcessor(environment));
    }

}
