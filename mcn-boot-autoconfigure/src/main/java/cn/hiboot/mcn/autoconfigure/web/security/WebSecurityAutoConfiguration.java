package cn.hiboot.mcn.autoconfigure.web.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * WebSecurityAutoConfiguration
 *
 * @author DingHao
 * @since 2021/5/23 23:36
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
@ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
@EnableConfigurationProperties(WebSecurityProperties.class)
public class WebSecurityAutoConfiguration extends WebSecurityConfigurerAdapter {

    private static final String[] IGNORE_PATH = {"/v2/api-docs", "/swagger-resources/**","/doc.html", "/webjars/**"};

    private final WebSecurityProperties webSecurityProperties;
    private final ObjectProvider<HttpSecurityConfigCustomizer> httpCustomizers;
    private final ObjectProvider<WebSecurityConfigCustomizer> webCustomizers;

    public WebSecurityAutoConfiguration(WebSecurityProperties webSecurityProperties,
                                        ObjectProvider<HttpSecurityConfigCustomizer> httpCustomizers,
                                        ObjectProvider<WebSecurityConfigCustomizer> webCustomizers){
        this.webSecurityProperties = webSecurityProperties;
        this.httpCustomizers = httpCustomizers;
        this.webCustomizers = webCustomizers;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        for (HttpSecurityConfigCustomizer httpCustomizer : httpCustomizers) {
            httpCustomizer.customize(http);
        }
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        if(webSecurityProperties.isEnableDefaultIgnore()){
            web.ignoring().antMatchers(IGNORE_PATH);
        }
        if(webSecurityProperties.getExcludes() != null){
            web.ignoring().antMatchers(webSecurityProperties.getExcludes());
        }
        for (WebSecurityConfigCustomizer webCustomizer : webCustomizers) {
            webCustomizer.customize(web);
        }
    }

}
