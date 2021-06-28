package cn.hiboot.mcn.autoconfigure.minio;

import io.minio.MinioClient;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/6/28 22:09
 */
public interface MinioClientBuilderCustomizer {
    void customize(MinioClient.Builder builder);
}
