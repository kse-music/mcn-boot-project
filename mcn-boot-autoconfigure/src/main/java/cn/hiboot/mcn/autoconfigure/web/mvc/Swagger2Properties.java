package cn.hiboot.mcn.autoconfigure.web.mvc;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/3/27 14:04
 */
@ConfigurationProperties("swagger")
public class Swagger2Properties {

    private boolean enable = false;

    private String title;
    private String description;
    private String termsOfServiceUrl;
    private String version;

    private String name;
    private String url;
    private String email;
    /**
     * 需扫描接口的包名,多个以逗号分隔
     */
    private List<String> packages;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
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

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }
}
