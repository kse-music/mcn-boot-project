package cn.hiboot.mcn.autoconfigure.web.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableConfigurationProperties(WebSecurityProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebSecurityAutoConfiguration {

    private final WebSecurityProperties webSecurityProperties;

    public WebSecurityAutoConfiguration(WebSecurityProperties webSecurityProperties){
        this.webSecurityProperties = webSecurityProperties;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web -> {
            if(webSecurityProperties.isEnableDefaultIgnore()){
                web.ignoring().antMatchers(webSecurityProperties.getDefaultExcludeUrls());
            }
            if(webSecurityProperties.getExcludeUrls() != null){
                web.ignoring().antMatchers(webSecurityProperties.getExcludeUrls());
            }
        };
    }

}
