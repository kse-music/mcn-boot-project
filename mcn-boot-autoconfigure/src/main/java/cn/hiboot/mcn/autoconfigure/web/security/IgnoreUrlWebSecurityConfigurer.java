package cn.hiboot.mcn.autoconfigure.web.security;

import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        List<String> urls = new ArrayList<>();
        if(webSecurityProperties.isEnableDefaultIgnore()){
            Collections.addAll(urls,webSecurityProperties.getDefaultExcludeUrls());
        }
        if(webSecurityProperties.getExcludeUrls() != null){
            Collections.addAll(urls,webSecurityProperties.getExcludeUrls());
        }
        if(McnUtils.isNotNullAndEmpty(urls)){
            web.ignoring().requestMatchers(new OrRequestMatcher(urls.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList())));
        }
    }

    @Override
    public int getOrder() {
        return webSecurityProperties.getOrder();
    }
}
