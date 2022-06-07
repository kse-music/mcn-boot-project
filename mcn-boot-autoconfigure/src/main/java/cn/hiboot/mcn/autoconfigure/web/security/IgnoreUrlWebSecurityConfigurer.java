package cn.hiboot.mcn.autoconfigure.web.security;

import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * IgnoreUrlWebSecurityConfigurerAdapter
 *
 * @author DingHao
 * @since 2022/6/7 20:53
 */
public class IgnoreUrlWebSecurityConfigurer extends WebSecurityConfigurerAdapter implements Ordered {

    private final WebSecurityProperties webSecurityProperties;

    public IgnoreUrlWebSecurityConfigurer(WebSecurityProperties webSecurityProperties){
        this.webSecurityProperties = webSecurityProperties;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        if(webSecurityProperties.isEnableDefaultIgnore()){
            web.ignoring().antMatchers(webSecurityProperties.getDefaultExcludeUrls());
        }
        if(webSecurityProperties.getExcludeUrls() != null){
            web.ignoring().antMatchers(webSecurityProperties.getExcludeUrls());
        }
    }

    @Override
    public int getOrder() {
        return webSecurityProperties.getOrder();
    }
}
