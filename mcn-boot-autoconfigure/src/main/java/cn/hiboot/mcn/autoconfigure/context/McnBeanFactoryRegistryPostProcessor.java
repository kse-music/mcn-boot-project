package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.autoconfigure.config.ComponentScanPackageCheck;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;

/**
 *
 * McnBeanFactoryRegistryPostProcessor
 * 修改IOC容器即添加AutowiredAnnotationBeanPostProcessor以处理McnAutowired注解
 *
 * @author DingHao
 * @since 2019/1/7 2:09
 */
public class McnBeanFactoryRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final ApplicationContext context;

    public McnBeanFactoryRegistryPostProcessor(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        new ComponentScanPackageCheck().check(registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.addBeanPostProcessor(new McnBeanPostProcessor(context));
    }

}
