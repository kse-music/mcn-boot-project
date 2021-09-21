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
     * feign断路器超时时间 默认10s
     */
    private Duration timeoutDuration = Duration.ofSeconds(10);

    /**
     *  当 future.get 超时时候（TimeoutException）,是否调用 future.cancel 取消异步任务  默认true
     */
    private boolean cancelRunningFuture = true;

    /**
     * feign日志打印级别默认FULL
     * 注意:feign的client日志级别只针对debug有效,需要单独设置client的level
     */
    private Logger.Level level = Logger.Level.FULL;

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
