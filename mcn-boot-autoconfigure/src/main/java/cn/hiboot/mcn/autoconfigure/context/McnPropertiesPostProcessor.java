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

public class McnPropertiesPostProcessor implements EnvironmentPostProcessor,Ordered {
    public static final String APP_BASE_PACKAGE = "app.base-package";
    private static final String MCN_SOURCE_NAME = "mcn-global-unique";
    private static final String MCN_DEFAULT_PROPERTY_SOURCE_NAME = "mcn-default";
    private static final String MCN_LOG_FILE_ENABLE = "mcn.log.file.enable";
    private static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = "bootstrap";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        //在引导上下文中也加载该源
        if (!environment.getPropertySources().contains(MCN_SOURCE_NAME)) {
            loadMcnConfigFile(environment);
        }
        if (notAppStart(environment)) {
            return;
        }
        if (environment.getPropertySources().contains(MCN_DEFAULT_PROPERTY_SOURCE_NAME)) {
            //already initialized
            return;
        }

        //add map config
        loadBasicConfig(environment, application);

        MutablePropertySources propertySources = environment.getPropertySources();

        //加载默认配置
        loadDefaultConfig(propertySources);

        //默认开启日志文件自动配置，在使用 Apollo以及nacos等配置中心可关闭以避免日志文件使用不到配置中心的配置
        boolean logFileEnable = environment.getProperty(MCN_LOG_FILE_ENABLE,Boolean.class,true);

        if (logFileEnable) {
            loadLogFileConfig(propertySources);
        }

    }

    private void loadBasicConfig(ConfigurableEnvironment environment, SpringApplication application){
        Map<String, Object> mapProp = new HashMap<>();
        Class<?> mainApplicationClass = application.getMainApplicationClass();//maybe is null
        if(Objects.nonNull(mainApplicationClass)){
            mapProp.put(APP_BASE_PACKAGE,ClassUtils.getPackageName(mainApplicationClass));
        }

        mapProp.put("mcn.log.file.name",environment.getProperty("mcn.log.file.name","error"));

        mapProp.put("mcn.version","v"+ this.getClass().getPackage().getImplementationVersion());
        Object abp = mapProp.get(APP_BASE_PACKAGE);
        if(abp != null){
            mapProp.put("logging.level."+abp+".dao","info");//do not println query statement
        }

        environment.getPropertySources().addLast(new MapPropertySource("mcn-map",mapProp));
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

    private void loadDefaultConfig(MutablePropertySources propertySources){
        ClassPathResource classPathResource = new ClassPathResource("mcn-default.properties", ConfigProperties.class);
        try{
            propertySources.addLast(new ResourcePropertySource(MCN_DEFAULT_PROPERTY_SOURCE_NAME,classPathResource));
        } catch (IOException e) {
            //ignore file not found
        }
    }

    private void loadLogFileConfig(MutablePropertySources propertySources){
        ClassPathResource classPathResource = new ClassPathResource("log.properties", ConfigProperties.class);
        try{
            propertySources.addLast(new ResourcePropertySource("mcn-log-file",classPathResource));
        } catch (IOException e) {
            //ignore file not found
        }
    }

    @Override
    public int getOrder() {
        return McnApplicationListener.DEFAULT_ORDER + 1;
    }

    private boolean notAppStart(ConfigurableEnvironment environment){
        return environment.getPropertySources().contains(BOOTSTRAP_PROPERTY_SOURCE_NAME);
    }

}
