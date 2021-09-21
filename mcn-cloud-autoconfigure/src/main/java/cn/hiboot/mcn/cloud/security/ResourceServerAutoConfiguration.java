package cn.hiboot.mcn.cloud.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * ResourceServerConfig
 *
 * @author DingHao
 * @since 2021/8/20 10:22
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({DefaultAuthenticationEventPublisher.class, JwtAuthenticationToken.class})
@ConditionalOnProperty(prefix = "mcn.resource.server",name = "enabled",havingValue = "true")
public class ResourceServerAutoConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and().oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
    }

}

