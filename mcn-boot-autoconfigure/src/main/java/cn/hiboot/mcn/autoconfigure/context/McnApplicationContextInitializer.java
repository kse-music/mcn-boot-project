package cn.hiboot.mcn.autoconfigure.context;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;

/**
 * McnApplicationContextInitializer
 * 应用上下文初始化器
 * 该初始化器是在IOC容器刷新前执行
 *
 * 将{@link McnBeanFactoryRegistryPostProcessor}添加到上下文中,在常规BDF加载后执行
 *
 * @author DingHao
 * @since 2021/1/16 16:41
 */
@Order(McnApplicationListener.DEFAULT_ORDER)
public class McnApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>{

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        configurableApplicationContext.addBeanFactoryPostProcessor(new McnBeanFactoryRegistryPostProcessor(configurableApplicationContext.getEnvironment()));
    }

}
