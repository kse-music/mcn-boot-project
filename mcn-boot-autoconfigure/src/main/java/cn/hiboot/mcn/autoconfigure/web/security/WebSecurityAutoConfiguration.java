package cn.hiboot.mcn.autoconfigure.web.security;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

/**
 * WebSecurityAutoConfiguration
 * 配置忽略的请求路径
 *
 * @author DingHao
 * @since 2021/5/23 23:36
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
@ConditionalOnBean(WebSecurityConfiguration.class)
@EnableConfigurationProperties(WebSecurityProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(SecurityAutoConfiguration.class)
public class WebSecurityAutoConfiguration implements WebSecurityCustomizer {

    private static final String[] IGNORE_PATH = {"/v2/api-docs", "/swagger-resources/**","/doc.html", "/webjars/**","/error","/favicon.ico"};

    private final WebSecurityProperties webSecurityProperties;

    public WebSecurityAutoConfiguration(WebSecurityProperties webSecurityProperties){
        this.webSecurityProperties = webSecurityProperties;
    }

    @Override
    public void customize(WebSecurity web) {
        if(webSecurityProperties.isEnableDefaultIgnore()){
            web.ignoring().antMatchers(IGNORE_PATH);
        }
        if(webSecurityProperties.getExcludes() != null){
            web.ignoring().antMatchers(webSecurityProperties.getExcludes());
        }
    }
}
