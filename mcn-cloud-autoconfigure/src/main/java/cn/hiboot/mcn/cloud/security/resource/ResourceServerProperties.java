package cn.hiboot.mcn.cloud.security.resource;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ResourceServerProperties
 *
 * @author DingHao
 * @since 2023/2/15 22:26
 */
@ConfigurationProperties(prefix = "sso")
public class ResourceServerProperties {

    private boolean opaqueToken;

    private String[] allowedPaths = {};

    private boolean verifyJwt = true;

    public boolean isOpaqueToken() {
        return opaqueToken;
    }

    public void setOpaqueToken(boolean opaqueToken) {
        this.opaqueToken = opaqueToken;
    }

    public String[] getAllowedPaths() {
        return allowedPaths;
    }

    public void setAllowedPaths(String[] allowedPaths) {
        this.allowedPaths = allowedPaths;
    }

    public boolean isVerifyJwt() {
        return verifyJwt;
    }

    public void setVerifyJwt(boolean verifyJwt) {
        this.verifyJwt = verifyJwt;
    }
}