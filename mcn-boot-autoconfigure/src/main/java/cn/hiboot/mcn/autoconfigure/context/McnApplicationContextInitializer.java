package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * McnApplicationContextInitializer
 * 应用上下文初始化器
 * 该初始化器是在IOC容器刷新前执行
 * 将{@link McnBeanFactoryRegistryPostProcessor}添加到上下文中,在常规BDF加载后执行
 *
 * @author DingHao
 * @since 2021/1/16 16:41
 */
@Order(McnApplicationListener.DEFAULT_ORDER)
public class McnApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>{

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        context.addBeanFactoryPostProcessor(new McnBeanFactoryRegistryPostProcessor(environment));
        Boolean swaggerEnabled = environment.getProperty("swagger.enabled", Boolean.class,false);
        if(Boolean.FALSE.equals(swaggerEnabled)){//since 3.0.0 for compatible swagger.enabled key
            environment.getPropertySources().addLast(new MapPropertySource("springDocPropertySource"
                    ,McnUtils.put("springdoc.api-docs.enabled",false,"springdoc.swagger-ui.enabled",false)));
        }
    }

}
