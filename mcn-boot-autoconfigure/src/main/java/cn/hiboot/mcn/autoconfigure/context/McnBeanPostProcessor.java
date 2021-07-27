package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.core.util.JacksonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 *
 * BeanPostProcessor
 *
 * @author DingHao
 * @since 2019/1/7 2:09
 */
public class McnBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ObjectMapper){
            JacksonUtils.setObjectMapper((ObjectMapper) bean);
        }
        return bean;
    }

}