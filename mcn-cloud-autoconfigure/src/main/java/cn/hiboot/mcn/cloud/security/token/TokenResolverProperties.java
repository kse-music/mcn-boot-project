package cn.hiboot.mcn.cloud.security.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TokenResolverProperties
 *
 * @author DingHao
 * @since 2024/10/14 11:08
 */
@ConfigurationProperties(prefix = "token.resolver")
public class TokenResolverProperties {

    private Long accessValidity = 12 * 60 * 60 * 1000L;

    public Long getAccessValidity() {
        return accessValidity;
    }

    public void setAccessValidity(Long accessValidity) {
        this.accessValidity = accessValidity;
    }

}
