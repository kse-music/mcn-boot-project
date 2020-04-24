package cn.hiboot.mcn.autoconfigure.web.jersey;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
   prefix = "jersey.swagger"
)
public class JerseySwaggerProperties {

    private String basePackage;
    private Boolean init = true;
    private Boolean xss = false;
    private Boolean authHeader = false;
    private String version;
    private String title = "API";
    private String host;
    private String ip;
    private Integer port;
    private String basePath = "";
    private String resourcePackage;
    /*
        指定单个资源(全路径)并将其注册进jersey
     */
    private String singleResource;
    /*
    扫描指定包下的资源并将其注册进jersey
     */
    private String otherResourcePackage;

    private String cdn;

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public Boolean getInit() {
        return init;
    }

    public void setInit(Boolean init) {
        this.init = init;
    }

    public Boolean getXss() {
        return xss;
    }

    public void setXss(Boolean xss) {
        this.xss = xss;
    }

    public Boolean getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(Boolean authHeader) {
        this.authHeader = authHeader;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getResourcePackage() {
        return resourcePackage;
    }

    public void setResourcePackage(String resourcePackage) {
        this.resourcePackage = resourcePackage;
    }

    public String getSingleResource() {
        return singleResource;
    }

    public void setSingleResource(String singleResource) {
        this.singleResource = singleResource;
    }

    public String getOtherResourcePackage() {
        return otherResourcePackage;
    }

    public void setOtherResourcePackage(String otherResourcePackage) {
        this.otherResourcePackage = otherResourcePackage;
    }

    public String getCdn() {
        return cdn;
    }

    public void setCdn(String cdn) {
        this.cdn = cdn;
    }
}
