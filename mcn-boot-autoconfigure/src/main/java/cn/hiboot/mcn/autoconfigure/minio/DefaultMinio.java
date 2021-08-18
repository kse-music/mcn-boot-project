package cn.hiboot.mcn.autoconfigure.minio;

import cn.hiboot.mcn.core.util.McnUtils;
import io.minio.MinioClient;

/**
 * Minio 工具类
 *
 * @author DingHao
 * @since 2021/6/28 22:13
 */
public class DefaultMinio implements Minio{

    private final MinioClient minioClient;

    private final String defaultBucketName;

    public DefaultMinio(MinioClient minioClient, String bucketName) {
        this.minioClient = minioClient;
        this.defaultBucketName = bucketName;
        if(McnUtils.isNotNullAndEmpty(bucketName)){
            //自动创建默认bucketName
            createBucket(bucketName);
        }
    }

    @Override
    public String getDefaultBucketName() {
        return defaultBucketName;
    }

    @Override
    public MinioClient getMinioClient() {
        return minioClient;
    }

}
