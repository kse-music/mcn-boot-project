package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.autoconfigure.config.ConfigProperties;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * McnEnvironmentPostProcessor
 * 环境后置处理器
 * 该初始化器是在IOC容器刷新前执行
 * <p>
 * McnEnvironmentPostProcessor主要给SpringBoot应用添加一些默认参数以及自定义一些唯一数据源和共享数据源
 * 另外还添加了一个即便在引导上下文中也在的数据源(方便一个项目中共享公共数据源)
 *
 * @author DingHao
 * @since 2021/1/16 16:46
 */
public class McnEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final boolean PRESENT = ClassUtils.isPresent("ch.qos.logback.classic.LoggerContext",McnEnvironmentPostProcessor.class.getClassLoader());
    private static final String CONSOLE_LOG_CHARSET = "CONSOLE_LOG_CHARSET";
    private static final String FILE_LOG_CHARSET = "FILE_LOG_CHARSET";

    private static final String BOOTSTRAP_EAGER_LOAD = "mcn.bootstrap.eagerLoad.enable";
    private static final String MCN_SOURCE_NAME = "mcn-global-unique";
    private static final String MCN_DEFAULT_PROPERTY_SOURCE_NAME = ConfigProperties.DEFAULT_PROPERTY_SOURCE_NAME;
    private static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = "bootstrap";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        //加载全局配置
        loadMcnConfigFile(environment);

        //引导上下文中默认不加载 默认配置和日志配置,但如果指定mcn.bootstrap.eagerLoad.enable=true则加载
        if (environment.getPropertySources().contains(BOOTSTRAP_PROPERTY_SOURCE_NAME) && !environment.getProperty(BOOTSTRAP_EAGER_LOAD, Boolean.class, false)) {
            return;
        }

        //加载默认配置
        loadDefaultConfig(environment, application.getMainApplicationClass());

        //兼容低版本
        setSystemProperty(environment, CONSOLE_LOG_CHARSET, "logging.charset.console", StandardCharsets.UTF_8.name());
        setSystemProperty(environment, FILE_LOG_CHARSET, "logging.charset.file", StandardCharsets.UTF_8.name());
    }

    private void setSystemProperty(PropertyResolver resolver, String systemPropertyName, String propertyName, String defaultValue) {
        String value = resolver.getProperty(propertyName);
        value = (value != null) ? value : defaultValue;
        System.setProperty(systemPropertyName, value);
    }

    /**
     * 无论在什么上下文中都加载的数据源,常见的就是Spring Cloud引导上下文
     *
     * @param environment 环境配置
     */
    private void loadMcnConfigFile(ConfigurableEnvironment environment) {
        if (environment.getPropertySources().contains(MCN_SOURCE_NAME)) {
            return;
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        //add global unique config file distinguish profile
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            for (int i = activeProfiles.length - 1; i >= 0; i--) {//后面申明的优先级高
                String activeProfile = activeProfiles[i];
                addLast(propertySources, loadResourcePropertySource(MCN_SOURCE_NAME.concat("-").concat(activeProfile), "classpath:config/mcn-" + activeProfile + ".properties"));
            }
        }

        addLast(propertySources, loadResourcePropertySource(MCN_SOURCE_NAME, "classpath:config/mcn.properties"));
    }

    /**
     * <p>1.如果默认配置已加载,则不再加载</p>
     * <p>2.如果是Spring Cloud引导上下文,则不加载,但如果指定参数mcn.bootstrap.eagerLoad.enable=true,则在引导上下文中也加载</p>
     *
     * @param environment 环境配置
     * @param mainApplicationClass 主类可能为null
     */
    private void loadDefaultConfig(ConfigurableEnvironment environment, Class<?> mainApplicationClass) {
        MutablePropertySources propertySources = environment.getPropertySources();
        //已加载
        if (propertySources.contains(MCN_DEFAULT_PROPERTY_SOURCE_NAME)) {
            return;
        }
        //add MapPropertySource,包含主源的包名,日志文件名,mcn版本,以及dao包下的日志打印级别
        Map<String, Object> mapProp = new HashMap<>();
        if (Objects.nonNull(mainApplicationClass)) {
            String packageName = ClassUtils.getPackageName(mainApplicationClass);
            mapProp.put(ConfigProperties.APP_BASE_PACKAGE, packageName);
            mapProp.put("logging.level." + packageName + ".dao", "info");//do not println query statement
            String projectVersion = McnUtils.getVersion(mainApplicationClass);
            if (projectVersion != null) {
                mapProp.put("project.version", projectVersion);
            }
        }
        mapProp.put("mcn.log.file.name", environment.getProperty("mcn.log.file.name", "error"));
        mapProp.put("mcn.version", "v" + McnUtils.getVersion(this.getClass()));
        addLast(propertySources, new MapPropertySource("mcn-map", mapProp));

        ResourcePropertySource propertySource = loadResourcePropertySource(MCN_DEFAULT_PROPERTY_SOURCE_NAME, ConfigProperties.mcnDefault());
        if(!PRESENT && propertySource != null){
            propertySource.getSource().remove("logging.pattern.console");
            propertySource.getSource().remove("logging.pattern.file");
        }
        addLast(propertySources, propertySource);

    }

    private ResourcePropertySource loadResourcePropertySource(String name, Object resource) {
        try {
            if (resource instanceof Resource) {
                return new ResourcePropertySource(name, (Resource) resource);
            }
            return new ResourcePropertySource(name, resource.toString());
        } catch (IOException e) {
            return null;
        }
    }

    private void addLast(MutablePropertySources propertySources, PropertySource<?> propertySource) {
        if (propertySource == null) {
            return;
        }
        propertySources.addLast(propertySource);
    }

    @Override
    public int getOrder() {
        return McnApplicationListener.DEFAULT_ORDER + 1;
    }

}