package cn.hiboot.mcn.autoconfigure.web.filter.integrity;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * DataIntegrityProperties
 *
 * @author DingHao
 * @since 2022/6/4 23:42
 */
@ConfigurationProperties("data.integrity")
public class DataIntegrityProperties {

    private boolean enable;
    private List<String> includePatterns = Collections.singletonList("/**");
    private List<String> excludePatterns = Collections.emptyList();
    private int order = -1000;
    private boolean checkUpload;
    private boolean checkReplay;
    private Duration timeout = Duration.ofMinutes(1);

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<String> getIncludePatterns() {
        return includePatterns;
    }

    public void setIncludePatterns(List<String> includePatterns) {
        this.includePatterns = includePatterns;
    }

    public List<String> getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(List<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isCheckUpload() {
        return checkUpload;
    }

    public void setCheckUpload(boolean checkUpload) {
        this.checkUpload = checkUpload;
    }

    public boolean isCheckReplay() {
        return checkReplay;
    }

    public void setCheckReplay(boolean checkReplay) {
        this.checkReplay = checkReplay;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
