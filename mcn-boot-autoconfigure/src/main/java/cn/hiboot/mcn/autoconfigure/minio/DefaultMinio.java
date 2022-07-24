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

    public DefaultMinio(DefaultMinioClient minioClient) {
        this.minioClient = minioClient;
        String defaultBucketName = minioClient.getConfig().getDefaultBucketName();
        if(McnUtils.isNullOrEmpty(defaultBucketName)){
            log.warn("It is recommended to set the default bucket name via minio.default-bucket-name");
        }else {
            //自动创建默认bucketName
            createBucket(defaultBucketName);
        }
    }

    @Override
    public DefaultMinioClient getMinioClient() {
        return minioClient;
    }

}
