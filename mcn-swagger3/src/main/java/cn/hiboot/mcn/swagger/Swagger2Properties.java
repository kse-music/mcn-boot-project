package cn.hiboot.mcn.swagger;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * swagger config
 *
 * @author DingHao
 * @since 2019/3/27 14:04
 */
@ConfigurationProperties("swagger")
public class Swagger2Properties {

    private String title;
    private String description;
    private String termsOfServiceUrl;
    private String version;

    private String name;
    private String url;
    private String email;

    /**
     * 是否生成请求头
     */
    private Header header = new Header();

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

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public static class Header {
        /**
         * 是否生成csrf请求参数头
         */
        private boolean csrf;
        /**
         * 是否生成authorization请求头
         */
        private Boolean authorization;

        public boolean isCsrf() {
            return csrf;
        }

        public void setCsrf(boolean csrf) {
            this.csrf = csrf;
        }

        public Boolean getAuthorization() {
            return authorization;
        }

        public void setAuthorization(Boolean authorization) {
            this.authorization = authorization;
        }
    }
}
