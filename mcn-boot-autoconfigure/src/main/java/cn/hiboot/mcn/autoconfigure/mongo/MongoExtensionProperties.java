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
    private boolean autoPojo;

    /**
     * 读取偏好设置
     */
    private ReadPreference readPreference = ReadPreference.primary;

    private WriteConcern writeConcern = WriteConcern.acknowledged;

    private ReadConcern readConcern;

    private Pool pool = new Pool();

    private Socket socket = new Socket();

    public boolean isAutoPojo() {
        return autoPojo;
    }

    public void setAutoPojo(boolean autoPojo) {
        this.autoPojo = autoPojo;
    }

    public ReadPreference getReadPreference() {
        return readPreference;
    }

    public void setReadPreference(ReadPreference readPreference) {
        this.readPreference = readPreference;
    }

    public WriteConcern getWriteConcern() {
        return writeConcern;
    }

    public void setWriteConcern(WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }

    public ReadConcern getReadConcern() {
        return readConcern;
    }

    public void setReadConcern(ReadConcern readConcern) {
        this.readConcern = readConcern;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public enum ReadPreference {
        primary,
        secondary,
        secondaryPreferred,
        primaryPreferred,
        nearest
    }

    public enum WriteConcern{
        acknowledged,
        w1,
        w2,
        w3,
        unacknowledged,
        journaled,
        majority
    }

    public enum ReadConcern{
        local,
        majority,
        linearizable,
        snapshot,
        available
    }

    public static class Pool{
        /**
         * 线程池允许的最大连接数,空闲时保存在池中
         */
        private int maxSize = 100;

        /**
         * 空闲时保存在池中最小连接数
         */
        private int minSize = 0;

        /**
         * 等待一个可用连接最大等待时间,单位ms
         */
        private long maxWaitTime = 120000;

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

        public long getMaxWaitTime() {
            return maxWaitTime;
        }

        public void setMaxWaitTime(long maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
        }
    }

    public static class Socket{

        /**
         * socket连接超时时间,单位ms
         */
        private int connectTimeout = 10000;

        /**
         * socket读取超时时间,单位ms
         */
        private int readTimeout;

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }
    }
}
