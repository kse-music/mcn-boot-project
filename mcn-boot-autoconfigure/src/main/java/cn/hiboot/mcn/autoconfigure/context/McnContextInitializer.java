package cn.hiboot.mcn.autoconfigure.context;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;

@Order(McnApplicationListener.DEFAULT_ORDER)
public class McnContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>{

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        configurableApplicationContext.addBeanFactoryPostProcessor(new McnBeanFactoryRegistryPostProcessor());
    }

}
