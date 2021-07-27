package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.core.service.McnAutowired;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.Collections;

/**
 *
 * BeanFactoryPostProcessor
 * 修改IOC容器即添加AutowiredAnnotationBeanPostProcessor以处理McnAutowired注解
 *
 * @author DingHao
 * @since 2019/1/7 2:09
 */
public class McnBeanFactoryRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //https://github.com/spring-projects/spring-framework/issues/24003
        AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
        autowiredAnnotationBeanPostProcessor.setAutowiredAnnotationTypes(Collections.singleton(McnAutowired.class));
        autowiredAnnotationBeanPostProcessor.setBeanFactory(beanFactory);
        beanFactory.addBeanPostProcessor(autowiredAnnotationBeanPostProcessor);
        beanFactory.addBeanPostProcessor(new McnBeanPostProcessor());
    }

}
