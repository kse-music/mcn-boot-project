package cn.hiboot.mcn.autoconfigure.web.security;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;

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
@Import(IgnoreUrlWebSecurityConfigurer.class)
public class WebSecurityAutoConfiguration {

    @Bean
    public ExceptionResolver securityExceptionResolver() {
        return new ExceptionResolver(){

            @Override
            public boolean support(HttpServletRequest request, Throwable t) {
                return t instanceof AccessDeniedException;
            }

            @Override
            public RestResp<Object> resolveException(HttpServletRequest request, Throwable t) {
                return RestResp.error(ExceptionKeys.HTTP_ERROR_403, t.getMessage());
            }

        };
    }

}
