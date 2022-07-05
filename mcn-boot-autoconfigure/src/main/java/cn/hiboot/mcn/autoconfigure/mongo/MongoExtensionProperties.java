package cn.hiboot.mcn.autoconfigure.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MongoExtensionProperties
 *
 * @author DingHao
 * @since 2022/7/5 13:08
 */
@ConfigurationProperties("mongo")
public class MongoExtensionProperties {

    /**
     * 读取偏好设置
     */
    private ReadPreference readPreference = ReadPreference.primary;
    /**
     * 线程池允许的最大连接数,空闲时保存在池中
     */
    private int maxSize = 100;
    /**
     * 空闲时保存在池中最小连接数
     */
    private int minSize = 0;
    /**
     * socket连接超时时间
     */
    private int connectTimeout = 10000;
    /**
     * 等待一个可用连接最大等待时间
     */
    private long maxWaitTime = 120000;

    public ReadPreference getReadPreference() {
        return readPreference;
    }

    public void setReadPreference(ReadPreference readPreference) {
        this.readPreference = readPreference;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public enum ReadPreference {
        primary,
        secondary,
        secondaryPreferred,
        primaryPreferred,
        nearest
    }
}
