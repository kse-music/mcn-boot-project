package cn.hiboot.mcn.cloud.security;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

/**
 * OAuth2ResourceServerConfigurerCustomizer
 *
 * @author DingHao
 * @since 2021/10/19 16:45
 */
public interface OAuth2ResourceServerConfigurerCustomizer {
    <H extends HttpSecurityBuilder<H>> void customize(OAuth2ResourceServerConfigurer<H> configurer);
}
