package cn.hiboot.mcn.autoconfigure.config;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.ClassUtils;

import java.io.IOException;

/**
 * WarConfiguration
 *
 * @author DingHao
 * @since 2022/7/12 10:59
 */
public class WarConfiguration extends SpringBootServletInitializer {

    private static String className;

    static {
        try {
           Object obj = new ResourcePropertySource("classpath:config/mcn.properties",WarConfiguration.class.getClassLoader()).getProperty("main.class");
           className = obj == null ? null : obj.toString();
        } catch (IOException e) {
            //ignore file not exist
        }
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        if(ClassUtils.isPresent(className,null)){
            builder.sources(ClassUtils.resolveClassName(className,null));
        }
        return builder;
    }

}
