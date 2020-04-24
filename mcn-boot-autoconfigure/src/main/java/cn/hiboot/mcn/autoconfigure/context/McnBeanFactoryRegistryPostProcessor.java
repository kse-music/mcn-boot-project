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
        //spring-framework issues/24003
//        beanFactory.getBeanDefinition(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME).getPropertyValues().add("mcnAutowired", McnAutowired.class);
//        beanFactory.addBeanPostProcessor(new McnBeanPostProcessor());
        AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
        autowiredAnnotationBeanPostProcessor.setAutowiredAnnotationTypes(Collections.singleton(McnAutowired.class));
        beanFactory.addBeanPostProcessor(autowiredAnnotationBeanPostProcessor);
    }

}
