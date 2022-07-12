package cn.hiboot.mcn.autoconfigure.config;

import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.util.ClassUtils;

import java.util.Properties;

/**
 * WarConfiguration
 *
 * @author DingHao
 * @since 2022/7/12 10:59
 */
public class WarConfiguration extends SpringBootServletInitializer {

    private static final String className;

    static {
        Properties properties = McnUtils.loadProperties("config/mcn.properties");
        className = properties.getProperty("main.class");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        if(ClassUtils.isPresent(className,null)){
            return builder.sources(ClassUtils.resolveClassName(className,null));
        }
        return builder.sources(EmptyConfiguration.class);
    }

    private static class EmptyConfiguration{

    }

}
