package cn.hiboot.mcn.autoconfigure.minio;

import cn.hiboot.mcn.core.util.McnUtils;

/**
 * Minio 工具类
 *
 * @author DingHao
 * @since 2021/6/28 22:13
 */
public class DefaultMinio implements Minio{

    private final DefaultMinioClient minioClient;

    private final MinioProperties config;

    public DefaultMinio(DefaultMinioClient minioClient, MinioProperties config) {
        this.minioClient = minioClient;
        this.config = config;
        String defaultBucketName = config.getDefaultBucketName();
        if(McnUtils.isNotNullAndEmpty(defaultBucketName)){
            //自动创建默认bucketName
            createBucket(defaultBucketName);
        }
    }

    @Override
    public DefaultMinioClient getMinioClient() {
        return minioClient;
    }

    @Override
    public MinioProperties getConfig() {
        return config;
    }

}
