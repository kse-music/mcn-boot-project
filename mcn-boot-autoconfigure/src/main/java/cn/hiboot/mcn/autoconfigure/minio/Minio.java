package cn.hiboot.mcn.autoconfigure.minio;

import cn.hiboot.mcn.core.util.McnUtils;
import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Minio 工具类
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
     * 刪除文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     */
    public void delete(String bucketName, String objectName) {
        try{
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            minioClient.removeObject(args);
        }catch (Exception e){
            log.error("delete failed {}",e.getMessage());
        }
    }

    /**
     * 文件列表
     *
     * @param bucketName 桶名称
     * @param recursive 列出所有文件包括子文件
     */
    public List<Item> listObjects(String bucketName,boolean recursive) {
        try{
            ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).recursive(recursive).build();
            Iterable<Result<Item>> list = minioClient.listObjects(args);
            return StreamSupport.stream(list.spliterator(), true).map(r -> {
                try {
                    return r.get();
                } catch (Exception e) {
                    //ignore
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }catch (Exception e){
            log.error("list all failed {}",e.getMessage());
        }
        return null;
    }

    public void deleteAll(String bucketName) {
        try{
            ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).recursive(true).build();
            Iterable<Result<Item>> list = minioClient.listObjects(args);
            List<DeleteObject> objectList = StreamSupport.stream(list.spliterator(), true).map(r -> {
                try {
                    return new DeleteObject(r.get().objectName());
                } catch (Exception e) {
                    //ignore
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            for (Result<DeleteError> errorResult : minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objectList).build())) {
                DeleteError error = errorResult.get();
                log.error("Error in deleting object {} ; {}", error.objectName(),error.message());
            }
        }catch (Exception e){
            log.error("delete all failed {}",e.getMessage());
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

    public List<BucketItem> listBuckets() {
        try{
            List<Bucket> buckets = minioClient.listBuckets();
            if(McnUtils.isNotNullAndEmpty(buckets)){
                return buckets.stream().map(b -> new BucketItem(b.name(),b.creationDate())).collect(Collectors.toList());
            }
        }catch (Exception e){
            log.error("create bucket failed {}",e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * 删除bucket
     * 该文件夹下不能有文件或子文件夹
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
