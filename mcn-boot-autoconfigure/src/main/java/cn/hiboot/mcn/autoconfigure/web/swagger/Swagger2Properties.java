package cn.hiboot.mcn.autoconfigure.web.swagger;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * swagger config
 *
 * @author DingHao
 * @since 2019/3/27 14:04
 */
@ConfigurationProperties("swagger")
public class Swagger2Properties {

    private boolean enabled = false;

    private String title;
    private String description;
    private String termsOfServiceUrl;
    private String version;

    private String name;
    private String url;
    private String email;

    private boolean csrf;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    public void setTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isCsrf() {
        return csrf;
    }

    public void setCsrf(boolean csrf) {
        this.csrf = csrf;
    }
}
