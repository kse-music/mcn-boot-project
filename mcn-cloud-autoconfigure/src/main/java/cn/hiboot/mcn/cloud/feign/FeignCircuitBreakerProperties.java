package cn.hiboot.mcn.cloud.feign;

import feign.Logger;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/9/21 13:43
 */
@ConfigurationProperties("mcn.feign")
@Setter
@Getter
public class FeignCircuitBreakerProperties {

    /**
     * feign断路器超时时间 默认2s
     */
    private Duration timeoutDuration = Duration.ofSeconds(2);

    /**
     * feign日志打印级别默认不打印
     */
    private Logger.Level level = Logger.Level.NONE;

    /**
     * 连接超时时间 默认10秒
     */
    private long connectTimeout = 10;

    /**
     * 都去超时时间 默认60秒
     */
    private long readTimeout = 60;

    private boolean followRedirects = true;



}
