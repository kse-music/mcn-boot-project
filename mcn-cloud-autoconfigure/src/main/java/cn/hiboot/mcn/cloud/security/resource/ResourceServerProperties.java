package cn.hiboot.mcn.cloud.security.resource;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * ResourceServerProperties
 *
 * @author DingHao
 * @since 2023/2/15 22:26
 */
@ConfigurationProperties(prefix = "sso")
public class ResourceServerProperties {
    private boolean opaqueToken;
    private List<String> allowedPaths;

    public boolean isOpaqueToken() {
        return opaqueToken;
    }

    public void setOpaqueToken(boolean opaqueToken) {
        this.opaqueToken = opaqueToken;
    }

    public List<String> getAllowedPaths() {
        return allowedPaths;
    }

    public void setAllowedPaths(List<String> allowedPaths) {
        this.allowedPaths = allowedPaths;
    }

}