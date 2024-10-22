package cn.hiboot.mcn.autoconfigure.minio;

import io.minio.http.HttpUtils;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

    private String externalEndpoint;

    private String accessKey;

    private String secretKey;

    /**
     * 默认bucket 不存在则新建
     */
    private String defaultBucketName;

    private String previewImageParameterName = "image";

    /**
     * 分块默认大小默认为10MB 最小值为5MB 最大值为5GB
     */
    private DataSize minMultipartSize = DataSize.ofMegabytes(10);

    /**
     * 预生成的url过期时间 默认6小时
     */
    private int expire = (int)TimeUnit.HOURS.toSeconds(6);

    /**
     * 上传方法
     */
    private String method = "PUT";

    private Pool pool = new Pool();

    private Client client = new Client();

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getExternalEndpoint() {
        return externalEndpoint;
    }

    public void setExternalEndpoint(String externalEndpoint) {
        this.externalEndpoint = externalEndpoint;
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

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public static class Pool{

        private static final int DEFAULT_SIZE = Runtime.getRuntime().availableProcessors();

        private Integer core = DEFAULT_SIZE;

        private Integer max = DEFAULT_SIZE;

        private int queueSize = 10;

        private String threadName = "Upload";

        public int getCore() {
            return core;
        }

        public void setCore(int core) {
            this.core = core;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }

        public String getThreadName() {
            return threadName;
        }

        public void setThreadName(String threadName) {
            this.threadName = threadName;
        }

    }

    public static class Client{
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

        public OkHttpClient okHttpClient(){
            return HttpUtils.newDefaultHttpClient(getConnectTimeout().toMillis(), getWriteTimeout().toMillis(), getReadTimeout().toMillis());
        }

    }

}
