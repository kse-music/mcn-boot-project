package cn.hiboot.mcn.autoconfigure.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

/**
 * minio配置项
 *
 * @author DingHao
 * @since 2021/6/28 22:03
 */
@ConfigurationProperties("minio")
public class MinioProperties {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(10);

    private String endpoint;

    private String accessKey;

    private String secretKey;

    /**
     * 默认bucket 不存在则新建
     */
    private String defaultBucketName;

    private String previewImageParameterName = "image";

    /**
     * 分块默认大小默认为5MB 最小值为5MB 最大值为5GB
     */
    private DataSize minMultipartSize = DataSize.ofMegabytes(5);

    /**
     * 连接服务器超时时间默认为10m
     */
    private Duration connectTimeout = DEFAULT_TIMEOUT;
    /**
     * 从服务器读取超时时间默认为10m
     */
    private Duration readTimeout = DEFAULT_TIMEOUT;
    /**
     * 写入服务器超时时间默认为10m
     */
    private Duration writeTimeout = DEFAULT_TIMEOUT;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getDefaultBucketName() {
        return defaultBucketName;
    }

    public void setDefaultBucketName(String defaultBucketName) {
        this.defaultBucketName = defaultBucketName;
    }

    public String getPreviewImageParameterName() {
        return previewImageParameterName;
    }

    public void setPreviewImageParameterName(String previewImageParameterName) {
        this.previewImageParameterName = previewImageParameterName;
    }

    public DataSize getMinMultipartSize() {
        return minMultipartSize;
    }

    public void setMinMultipartSize(DataSize minMultipartSize) {
        this.minMultipartSize = minMultipartSize;
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

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }
}
