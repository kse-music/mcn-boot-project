package cn.hiboot.mcn.autoconfigure.minio;

import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/6/28 22:13
 */
public class Minio {

    private static final Logger log = LoggerFactory.getLogger(Minio.class);

    private final MinioClient minioClient;

    public Minio(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 文件上传
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param stream     文件流
     */
    public void upload(String bucketName, String objectName, InputStream stream) {
        try{
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(stream, stream.available(), -1)
                    .build();
            minioClient.putObject(args);
        }catch (Exception e){
            log.error("upload failed {}",e.getMessage());
        }
    }

    /**
     * 获取文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return 二进制流
     */
    public InputStream getObject(String bucketName, String objectName) {
        try{
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            return minioClient.getObject(args);
        }catch (Exception e){
            log.error("acquire failed {}",e.getMessage());
        }
        return null;

    }

    /**
     * 创建bucket
     * @param bucketName bucket名称
     */
    public void createBucket(String bucketName) {
        try{
            MakeBucketArgs args = MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            minioClient.makeBucket(args);
        }catch (Exception e){
            log.error("create bucket failed {}",e.getMessage());
        }
    }

    /**
     * 删除bucket
     * @param bucketName bucket名称
     */
    public void deleteBucket(String bucketName) {
        try{
            RemoveBucketArgs args = RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            minioClient.removeBucket(args);
        }catch (Exception e){
            log.error("create bucket failed {}",e.getMessage());
        }
    }

}
