package cn.hiboot.mcn.cloud.feign;

import feign.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * FeignCircuitBreakerProperties
 *
 * @author DingHao
 * @since 2021/9/21 13:43
 */
@ConfigurationProperties("mcn.feign")
public class FeignExtensionProperties {


    /**
     * feign日志打印级别默认FULL
     * 注意:feign的client日志级别只针对debug有效,需要单独设置client的level
     */
    private Logger.Level level = Logger.Level.FULL;

    /**
     * 连接超时时间 默认10秒
     */
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * 读取超时时间 默认60秒
     */
    private Duration readTimeout = Duration.ofSeconds(60);

    private boolean followRedirects = true;

    public Logger.Level getLevel() {
        return level;
    }

    public void setLevel(Logger.Level level) {
        this.level = level;
    }

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

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }
}
