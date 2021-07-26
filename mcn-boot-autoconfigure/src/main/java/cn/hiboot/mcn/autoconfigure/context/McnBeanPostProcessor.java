package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.autoconfigure.web.validator.LocalValidatorCustomizer;
import cn.hiboot.mcn.core.util.JacksonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 *
 * BeanPostProcessor
 *
 * @author DingHao
 * @since 2019/1/7 2:09
 */
public class McnBeanPostProcessor implements BeanPostProcessor {

    private final BeanFactory beanFactory;

    public McnBeanPostProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof LocalValidatorFactoryBean){
            LocalValidatorFactoryBean b = (LocalValidatorFactoryBean) bean;
            if(!b.getValidationPropertyMap().containsKey("hibernate.validator.fail_fast")){
                b.getValidationPropertyMap().put("hibernate.validator.fail_fast", "true");//default use fast fail
            }
            for (LocalValidatorCustomizer localValidatorCustomizer : beanFactory.getBeanProvider(LocalValidatorCustomizer.class)) {
                localValidatorCustomizer.customize(b);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ObjectMapper){
            JacksonUtils.setObjectMapper((ObjectMapper) bean);
        }
        return bean;
    }

}