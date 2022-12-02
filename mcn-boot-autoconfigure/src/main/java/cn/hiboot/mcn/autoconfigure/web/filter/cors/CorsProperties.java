package cn.hiboot.mcn.autoconfigure.web.filter.cors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * CorsProperties
 *
 * @author DingHao
 * @since 2019/1/9 11:34
 */
@ConfigurationProperties("mcn.cors")
public class CorsProperties {

    private String name = "defaultCorsFilter";
    private String pattern = "/**";
    private Boolean allowCredentials;
    private String allowedOrigin = CorsConfiguration.ALL;
    private String allowedHeader = CorsConfiguration.ALL;
    private String allowedMethod = CorsConfiguration.ALL;
    private List<String> exposedHeaders;
    private Long maxAge = 3600L;
    private int order = -1000;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public String getAllowedOrigin() {
        return allowedOrigin;
    }

    public void setAllowedOrigin(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    public String getAllowedHeader() {
        return allowedHeader;
    }

    public void setAllowedHeader(String allowedHeader) {
        this.allowedHeader = allowedHeader;
    }

    public String getAllowedMethod() {
        return allowedMethod;
    }

    public void setAllowedMethod(String allowedMethod) {
        this.allowedMethod = allowedMethod;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public Long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
