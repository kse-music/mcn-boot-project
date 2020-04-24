package cn.hiboot.mcn.autoconfigure.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

@ConfigurationProperties("jwt")
public class JwtProperties {
    private String secretKey = "SECRET";
    private String issuer = "hiekn";
    private Long refreshInterval = 3L;
    private Integer unit = 1000;
    private Integer expireDate = 14*24*3600;
    @NestedConfigurationProperty
    private Security security = new Security();

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public Integer getUnit() {
        return unit;
    }

    public void setUnit(Integer unit) {
        this.unit = unit;
    }

    public Integer getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Integer expireDate) {
        this.expireDate = expireDate;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public static class Security{
        private Boolean login = false;
        private List<String> ignoreUrls;

        public Boolean getLogin() {
            return login;
        }

        public void setLogin(Boolean login) {
            this.login = login;
        }

        public List<String> getIgnoreUrls() {
            return ignoreUrls;
        }

        public void setIgnoreUrls(List<String> ignoreUrls) {
            this.ignoreUrls = ignoreUrls;
        }
    }
}
