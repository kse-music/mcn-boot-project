package cn.hiboot.mcn.cloud.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * ResourceServerConfig
 *
 * @author DingHao
 * @since 2021/8/20 10:22
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({DefaultAuthenticationEventPublisher.class, JwtAuthenticationToken.class, JwtDecoder.class})
@ConditionalOnDefaultWebSecurity
public class ResourceServerAutoConfiguration extends WebSecurityConfigurerAdapter {

    private final ObjectProvider<OAuth2ResourceServerConfigurerCustomizer> customizers;

    public ResourceServerAutoConfiguration(ObjectProvider<OAuth2ResourceServerConfigurerCustomizer> customizers) {
        this.customizers = customizers;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and().oauth2ResourceServer(o -> applyCustom(o.jwt().and()));
    }

    private <H extends HttpSecurityBuilder<H>> void applyCustom(OAuth2ResourceServerConfigurer<H> configurer){
        for (OAuth2ResourceServerConfigurerCustomizer customizer : customizers) {
            customizer.customize(configurer);
        }
    }

}
