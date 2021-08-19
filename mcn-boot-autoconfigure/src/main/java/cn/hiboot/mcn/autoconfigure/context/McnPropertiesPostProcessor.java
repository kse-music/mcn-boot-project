package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.autoconfigure.web.config.ConfigProperties;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private static final String MCN_LOG_FILE_EAGER_LOAD = "mcn.log.file.eagerLoad";
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
        //add global unique config file diff profile
        String[] activeProfiles = environment.getActiveProfiles();
        if(activeProfiles.length > 0){
            for (int i = activeProfiles.length - 1; i >= 0; i--) {//后面申明的优先级高
                String activeProfile = activeProfiles[i];
                addLast(propertySources,loadResourcePropertySource(MCN_SOURCE_NAME.concat("-").concat(activeProfile),"classpath:config/mcn-"+activeProfile+".properties"));
            }
        }
        //总是加载mcn配置文件
        addLast(propertySources,loadResourcePropertySource(MCN_SOURCE_NAME, "classpath:config/mcn.properties"));
    }

    private void loadDefaultConfig(ConfigurableEnvironment environment, SpringApplication application){
        MutablePropertySources propertySources = environment.getPropertySources();

        //add MapPropertySource,包含主源的包名,日志文件名,mcn版本,以及dao包下的日志打印级别
        Map<String, Object> mapProp = new HashMap<>();
        Class<?> mainApplicationClass = application.getMainApplicationClass();//maybe is null
        if(Objects.nonNull(mainApplicationClass)){
            String packageName = ClassUtils.getPackageName(mainApplicationClass);
            mapProp.put(APP_BASE_PACKAGE, packageName);
            mapProp.put("logging.level."+packageName+".dao","info");//do not println query statement
            String projectVersion =McnUtils.getVersion(mainApplicationClass);
            if(projectVersion != null){
                mapProp.put("project.version","v"+ projectVersion);
            }
        }
        mapProp.put("mcn.log.file.name",environment.getProperty("mcn.log.file.name","error"));
        mapProp.put("mcn.version","v"+ McnUtils.getVersion(this.getClass()));
        addLast(propertySources,new MapPropertySource("mcn-map",mapProp));

        //加载默认配置
        addLast(propertySources,loadResourcePropertySource(MCN_DEFAULT_PROPERTY_SOURCE_NAME, loadClassPathResource("mcn-default.properties")));

        /*
         * 1.nacos使用的是ApplicationContextInitializer(PropertySourceBootstrapConfiguration)启动加载的,优先级低于EnvironmentPostProcessor,
         * 所以此时McnPropertiesPostProcessor还读不到nacos中的配置
         * 2.PropertySourceBootstrapConfiguration是需要Bootstrap上下文引导的,但是在spring  boot 2.4+默认不启动Bootstrap上下文
         * 3.如果nacos中配置了logging开头的属性会触发重新初始化日志
         * 4.如果此时nacos上配置的日志文件和mcn默认不一样就会生成两个文件
         */
        boolean logFileEnable = environment.getProperty(MCN_LOG_FILE_EAGER_LOAD,Boolean.class,false);

        //手动开启日志配置或者没有nacos的情况下触发mcn默认日志配置
        //此时nacos上必须配置了logging否则整个项目不会有日志文件的配置或者在配置文件中配置mcn.log.file.eagerLoad=true
        if (logFileEnable || withoutPropertySourceLocator(application.getInitializers())) {
            //加载默认日志配置
            addLast(propertySources,loadResourcePropertySource("mcn-log-file", loadClassPathResource("log.properties")));
        }

    }

    private boolean withoutPropertySourceLocator(Set<ApplicationContextInitializer<?>> initializers){
        boolean hasPropertySourceLocator = ClassUtils.isPresent("com.alibaba.cloud.nacos.client.NacosPropertySourceLocator",null);
        boolean hasPropertySourceBootstrapConfiguration = false;
        for (ApplicationContextInitializer<?> initializer : initializers) {
            if(initializer.getClass().getName().equals("org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration")){
                hasPropertySourceBootstrapConfiguration = true;
                break;
            }
        }
        return !hasPropertySourceLocator || !hasPropertySourceBootstrapConfiguration;
    }

    private ClassPathResource loadClassPathResource(String file){
        return new ClassPathResource(file, ConfigProperties.class);
    }

    private ResourcePropertySource loadResourcePropertySource(String name, Object resource){
        try {
            if(resource instanceof Resource){
                return new ResourcePropertySource(name, (Resource)resource);
            }
            return new ResourcePropertySource(name, resource.toString());
        } catch (IOException e) {
            //ignore resource not exist
        }
        return null;
    }

    private void addLast(MutablePropertySources propertySources, PropertySource<?> propertySource){
        if(propertySource != null){
            propertySources.addLast(propertySource);
        }
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
