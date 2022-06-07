package cn.hiboot.mcn.autoconfigure.web.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    @Order(-10000)
    public Filter webSecurityCustomizer(){
        List<RequestMatcher> requestMatchers = new ArrayList<>();
        if(webSecurityProperties.isEnableDefaultIgnore()){
            requestMatchers.addAll(Arrays.stream(webSecurityProperties.getDefaultExcludeUrls()).map(AntPathRequestMatcher::new).collect(Collectors.toList()));
        }
        if(webSecurityProperties.getExcludeUrls() != null){
            requestMatchers.addAll(Arrays.stream(webSecurityProperties.getExcludeUrls()).map(AntPathRequestMatcher::new).collect(Collectors.toList()));
        }
       RequestMatcher requestMatcher = new OrRequestMatcher(requestMatchers);
        return (request, response,chain) -> {
            if(requestMatcher.matches((HttpServletRequest) request)){
                return;
            }
            chain.doFilter(request,response);
        };
    }

}
