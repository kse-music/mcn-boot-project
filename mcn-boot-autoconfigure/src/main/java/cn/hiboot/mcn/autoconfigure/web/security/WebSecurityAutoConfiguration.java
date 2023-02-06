package cn.hiboot.mcn.autoconfigure.web.security;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSecurityAutoConfiguration
 * 配置忽略的请求路径
 *
 * @author DingHao
 * @since 2021/5/23 23:36
 */
@AutoConfiguration
@ConditionalOnClass(WebSecurity.class)
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
        };
    }

    @Bean
    public ExceptionResolver<AccessDeniedException> securityExceptionResolver() {
        return t -> RestResp.error(ExceptionKeys.HTTP_ERROR_403, t.getMessage());
    }

}
