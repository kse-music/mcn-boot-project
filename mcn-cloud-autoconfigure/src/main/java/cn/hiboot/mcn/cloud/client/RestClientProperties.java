package cn.hiboot.mcn.cloud.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * RestTemplateProperties
 *
 * @author DingHao
 * @since 2023/1/3 14:54
 */
@ConfigurationProperties(prefix = "rest.template")
public class RestClientProperties {

    private Duration connectTimeout = Duration.ofMillis(10000);
    private Duration readTimeout = Duration.ofMillis(60000);

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}
