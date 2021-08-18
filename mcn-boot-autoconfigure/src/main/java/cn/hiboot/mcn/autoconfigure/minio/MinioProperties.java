package cn.hiboot.mcn.autoconfigure.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * minio配置项
 *
 * @author DingHao
 * @since 2021/6/28 22:03
 */
@ConfigurationProperties("minio")
public class MinioProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String defaultBucketName;

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
}
