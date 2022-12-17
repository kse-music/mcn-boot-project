package cn.hiboot.mcn.autoconfigure.sql;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import javax.sql.DataSource;

/**
 * DataSourceInitializerPostProcessor
 *
 * @author DingHao
 * @since 2022/12/17 21:57
 */
class DataSourceInitializerPostProcessor implements BeanPostProcessor, BeanFactoryAware,Ordered {

    private BeanFactory beanFactory;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource) {
            this.beanFactory.getBean(DataSourceInitializerInvoker.class);
        }
        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
