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
public interface Minio {

    String DEFAULT_PREVIEW_PARAMETER_NAME = "image";

    Logger log = LoggerFactory.getLogger(DefaultMinio.class);

    String getDefaultBucketName();
    
    MinioClient getMinioClient();

    String getPreviewParameterName();

    default void upload(String objectName, InputStream stream){
        upload(getDefaultBucketName(),objectName,stream);
    }
    /**
     * 文件上传
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param stream     文件流
     */
    default void upload(String bucketName, String objectName, InputStream stream) {
        try{
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(stream, stream.available(), -1)
                    .build();
            getMinioClient().putObject(args);
        }catch (Exception e){
            throw new MinioException(e);
        }
    }

    default void delete(String objectName){
        delete(getDefaultBucketName(),objectName);
    }
    /**
     * 刪除文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     */
    default void delete(String bucketName, String objectName) {
        try{
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            getMinioClient().removeObject(args);
        }catch (Exception e){
            throw new MinioException(e);
        }
    }

    default List<Item> listObjects(boolean recursive){
        return listObjects(getDefaultBucketName(),recursive);
    }
    /**
     * 文件列表
     *
     * @param bucketName 桶名称
     * @param recursive 列出所有文件包括子文件
     * @return item
     */
    default List<Item> listObjects(String bucketName,boolean recursive) {
        try{
            ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).recursive(recursive).build();
            Iterable<Result<Item>> list = getMinioClient().listObjects(args);
            return StreamSupport.stream(list.spliterator(), true).map(r -> {
                try {
                    return r.get();
                } catch (Exception e) {
                    //ignore
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }catch (Exception e){
            throw new MinioException(e);
        }
    }

    default void deleteAll(){
        deleteAll(getDefaultBucketName());
    }

    default void deleteAll(String bucketName) {
        try{
            ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).recursive(true).build();
            Iterable<Result<Item>> list = getMinioClient().listObjects(args);
            List<DeleteObject> objectList = StreamSupport.stream(list.spliterator(), true).map(r -> {
                try {
                    return new DeleteObject(r.get().objectName());
                } catch (Exception e) {
                    //ignore
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            for (Result<DeleteError> errorResult : getMinioClient().removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objectList).build())) {
                DeleteError error = errorResult.get();
                log.error("Error delete object {} ; {}", error.objectName(),error.message());
            }
        }catch (Exception e){
            throw new MinioException(e);
        }
    }

    default InputStream getObject(String objectName){
        return getObject(getDefaultBucketName(),objectName);
    }
    /**
     * 获取文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return 二进制流
     */
    default InputStream getObject(String bucketName, String objectName) {
        try{
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            return getMinioClient().getObject(args);
        }catch (Exception e){
            throw new MinioException(e);
        }
    }

    default boolean buckExist(String bucketName){
        try {
            return getMinioClient().bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    /**
     * 创建bucket
     * @param bucketName bucket名称
     */
    default void createBucket(String bucketName) {
        if(buckExist(bucketName)){
            return;
        }
        try{
            MakeBucketArgs args = MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            getMinioClient().makeBucket(args);
        }catch (Exception e){
            throw new MinioException(e);
        }
    }

    default List<BucketItem> listBuckets() {
        try{
            List<Bucket> buckets = getMinioClient().listBuckets();
            if(McnUtils.isNotNullAndEmpty(buckets)){
                return buckets.stream().map(b -> new BucketItem(b.name(),b.creationDate())).collect(Collectors.toList());
            }
        }catch (Exception e){
            throw new MinioException(e);
        }
        return Collections.emptyList();
    }

    /**
     * 删除bucket
     * 该文件夹下不能有文件或子文件夹
     * @param bucketName bucket名称
     */
    default void deleteBucket(String bucketName) {
        try{
            RemoveBucketArgs args = RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            getMinioClient().removeBucket(args);
        }catch (Exception e){
            throw new MinioException(e);
        }
    }

}
