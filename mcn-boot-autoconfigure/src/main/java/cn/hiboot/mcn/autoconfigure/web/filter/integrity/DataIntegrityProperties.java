package cn.hiboot.mcn.autoconfigure.web.filter.integrity;

import org.springframework.boot.context.properties.ConfigurationProperties;

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

    private boolean check;
    private List<String> includePatterns = Collections.singletonList("/**");
    private List<String> excludePatterns = Collections.emptyList();
    private int order = -1000;

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
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
}
