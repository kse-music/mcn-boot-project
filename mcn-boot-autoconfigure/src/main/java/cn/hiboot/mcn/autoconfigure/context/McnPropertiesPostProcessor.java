package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.autoconfigure.web.config.ConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * EnvironmentPostProcessor
 * 环境后置处理器
 * 该初始化器是在IOC容器刷新前执行
 *
 * McnPropertiesPostProcessor主要给SpringBoot应用添加一些默认参数以及自定义一些唯一数据源和共享数据源
 * 另外还添加了一个即便在引导上下文中也在的数据源(方便一个项目中共享公共数据源)
 *
 * @author DingHao
 * @since 2021/1/16 16:46
 */
public class McnPropertiesPostProcessor implements EnvironmentPostProcessor,Ordered {

    private static final String BOOTSTRAP_EAGER_LOAD = "mcn.bootstrap.eagerLoad.enable";
    public static final String APP_BASE_PACKAGE = "app.base-package";
    private static final String MCN_SOURCE_NAME = "mcn-global-unique";
    private static final String MCN_DEFAULT_PROPERTY_SOURCE_NAME = "mcn-default";
    private static final String MCN_LOG_FILE_ENABLE = "mcn.log.file.enable";
    private static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = "bootstrap";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        //无论在什么上下文中都加载的数据源,常见的就是Spring Cloud引导上下文
        if (!environment.getPropertySources().contains(MCN_SOURCE_NAME)) {
            loadMcnConfigFile(environment);
        }

        //如果是Spring Cloud引导上下文不加载后面的配置,可通过在bootstrap.properties指定参数mcn.bootstrap.eagerLoad.enable=true
        if (needNotRun(environment)) {
            return;
        }

        //如果默认配置已加载
        if (environment.getPropertySources().contains(MCN_DEFAULT_PROPERTY_SOURCE_NAME)) {
            //already initialized
            return;
        }

        //加载默认配置
        loadDefaultConfig(environment,application);

    }

    private void loadMcnConfigFile(ConfigurableEnvironment environment){
        MutablePropertySources propertySources = environment.getPropertySources();
        try {
            //add global unique config file
            propertySources.addLast(new ResourcePropertySource(MCN_SOURCE_NAME,"classpath:config/mcn.properties"));
        } catch (IOException e) {
            //ignore file not found
        }
    }

    private void loadDefaultConfig(ConfigurableEnvironment environment, SpringApplication application){
        MutablePropertySources propertySources = environment.getPropertySources();

        //add MapPropertySource,包含主源的包名,日志文件名,mcn版本,以及dao包下的日志打印级别
        loadMapConfig(environment, application);

        try{
            propertySources.addLast(new ResourcePropertySource(MCN_DEFAULT_PROPERTY_SOURCE_NAME,loadClassPathResource("mcn-default.properties")));
            //默认开启日志文件自动配置，在使用 Apollo以及nacos等配置中心可关闭以避免日志文件使用不到配置中心的配置
            boolean logFileEnable = environment.getProperty(MCN_LOG_FILE_ENABLE,Boolean.class,true);
            if (logFileEnable) {
                propertySources.addLast(new ResourcePropertySource("mcn-log-file",loadClassPathResource("log.properties")));
            }
        } catch (IOException e) {
            //ignore file not found
        }

    }

    private void loadMapConfig(ConfigurableEnvironment environment, SpringApplication application){
        Map<String, Object> mapProp = new HashMap<>();
        Class<?> mainApplicationClass = application.getMainApplicationClass();//maybe is null
        if(Objects.nonNull(mainApplicationClass)){
            String packageName = ClassUtils.getPackageName(mainApplicationClass);
            mapProp.put(APP_BASE_PACKAGE, packageName);
            mapProp.put("logging.level."+packageName+".dao","info");//do not println query statement
        }
        mapProp.put("mcn.log.file.name",environment.getProperty("mcn.log.file.name","error"));
        mapProp.put("mcn.version","v"+ this.getClass().getPackage().getImplementationVersion());
        environment.getPropertySources().addLast(new MapPropertySource("mcn-map",mapProp));
    }

    private ClassPathResource loadClassPathResource(String file){
        return new ClassPathResource(file, ConfigProperties.class);
    }

    @Override
    public int getOrder() {
        return McnApplicationListener.DEFAULT_ORDER + 1;
    }

    private boolean needNotRun(ConfigurableEnvironment environment){
        if(environment.getProperty(BOOTSTRAP_EAGER_LOAD, Boolean.class, false)){
            return false;
        }
        return environment.getPropertySources().contains(BOOTSTRAP_PROPERTY_SOURCE_NAME);
    }

}
