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
import java.util.Map;
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

    Logger log = LoggerFactory.getLogger(Minio.class);

    Map<String, String> map = Collections.singletonMap("Expect", "100-continue");

    DefaultMinioClient getMinioClient();

    default String getFilePath(String fileName) {
        return getFilePath(getDefaultBucketName(), fileName);
    }

    default String getFilePath(String bucketName, String fileName) {
        return String.format("%s/%s/%s", getMinioClient().getConfig().getEndpoint(), bucketName, fileName);
    }

    default String getDefaultBucketName() {
        return getMinioClient().getConfig().getDefaultBucketName();
    }

    default String getOrDefaultBucket(String bucketName) {
        if (McnUtils.isNullOrEmpty(bucketName)) {
            bucketName = getDefaultBucketName();
        }
        if (McnUtils.isNullOrEmpty(bucketName)) {
            throw new MinioException(bucketName + " must not be null or empty");
        }
        return bucketName;
    }

    default void upload(String objectName, long objectSize, InputStream stream) {
        upload(objectName, objectSize, null, stream);
    }

    default void upload(String objectName, long objectSize, String contentType, InputStream stream) {
        upload(null, objectName, objectSize, 0, contentType, stream);
    }

    /**
     * 文件上传
     *
     * @param bucketName  桶名称
     * @param objectName  文件名
     * @param objectSize  文件大小
     * @param partSize    分片大小
     * @param contentType contentType
     * @param stream      文件流
     */
    default void upload(String bucketName, String objectName, long objectSize, long partSize, String contentType, InputStream stream) {
        bucketName = getOrDefaultBucket(bucketName);
        DefaultMinioClient minioClient = getMinioClient();
        if (partSize == 0) {
            partSize = minioClient.getConfig().getMinMultipartSize().toBytes();
        }
        try {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .headers(map)
                    .stream(stream, objectSize, partSize);
            if (McnUtils.isNotNullAndEmpty(contentType)) {
                builder.contentType(contentType);
            }
            minioClient.putObject(builder.build()).get();
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default void uploadParallel(String objectName, long objectSize, InputStream stream) {
        uploadParallel(objectName, objectSize, null, stream);
    }

    default void uploadParallel(String objectName, long objectSize, String contentType, InputStream stream) {
        uploadParallel(null, objectName, objectSize, contentType, stream);
    }

    default void uploadParallel(String bucketName, String objectName, long objectSize, String contentType, InputStream stream) {
        bucketName = getOrDefaultBucket(bucketName);
        try {
            getMinioClient().upload(bucketName, objectName, objectSize, contentType, stream);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default PreSignResult presignedObjectUrl(String objectName, String uploadId, int count) {
        return presignedObjectUrl(null, objectName, uploadId, null, count);
    }

    default PreSignResult presignedObjectUrl(String objectName, String uploadId, String contentType, int count) {
        return presignedObjectUrl(null, objectName, uploadId, contentType, count);
    }

    default PreSignResult presignedObjectUrl(String bucketName, String objectName, String uploadId, String contentType, int count) {
        bucketName = getOrDefaultBucket(bucketName);
        try {
            return getMinioClient().getPresignedObjectUrl(bucketName, objectName, uploadId, contentType, count);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default List<Integer> listParts(String objectName, String uploadId) {
        return listParts(null, objectName, uploadId);
    }

    default List<Integer> listParts(String bucketName, String objectName, String uploadId) {
        bucketName = getOrDefaultBucket(bucketName);
        try {
            return getMinioClient().listParts(bucketName, objectName, uploadId);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default void mergeMultipartUpload(String objectName, String uploadId) {
        mergeMultipartUpload(null, objectName, uploadId);
    }

    default void mergeMultipartUpload(String bucketName, String objectName, String uploadId) {
        bucketName = getOrDefaultBucket(bucketName);
        try {
            getMinioClient().mergeMultipartUpload(bucketName, objectName, uploadId);
        } catch (MinioException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default void delete(String objectName) {
        delete(null, objectName);
    }

    /**
     * 刪除文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     */
    default void delete(String bucketName, String objectName) {
        bucketName = getOrDefaultBucket(bucketName);
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            getMinioClient().removeObject(args).get();
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default List<Item> listObjects() {
        return listObjects(null, false);
    }

    default List<Item> listObjects(boolean recursive) {
        return listObjects(null, recursive);
    }

    default List<Item> listObjects(String bucketName) {
        return listObjects(bucketName, false);
    }

    /**
     * 文件列表
     *
     * @param bucketName 桶名称
     * @param recursive  列出所有文件包括子文件
     * @return item
     */
    default List<Item> listObjects(String bucketName, boolean recursive) {
        bucketName = getOrDefaultBucket(bucketName);
        try {
            ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).recursive(recursive).build();
            Iterable<Result<Item>> list = getMinioClient().listObjects(args);
            return StreamSupport.stream(list.spliterator(), true).map(r -> {
                try {
                    return r.get();
                } catch (Exception ignored) {
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default void deleteAll() {
        deleteAll(null);
    }

    default void deleteAll(String bucketName) {
        bucketName = getOrDefaultBucket(bucketName);
        try {
            ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).recursive(true).build();
            Iterable<Result<Item>> list = getMinioClient().listObjects(args);
            List<DeleteObject> objectList = StreamSupport.stream(list.spliterator(), true).map(r -> {
                try {
                    return new DeleteObject(r.get().objectName());
                } catch (Exception ignored) {
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            for (Result<DeleteError> errorResult : getMinioClient().removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objectList).build())) {
                DeleteError error = errorResult.get();
                log.error("Error delete object {} ; Reason is {}", error.objectName(), error.message());
            }
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default InputStream getObject(String objectName) {
        return getObject(objectName, null, null);
    }

    default InputStream getObject(String objectName, Long offset, Long length) {
        return getObject(null, objectName, offset, length);
    }

    /**
     * 获取文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return 二进制流
     */
    default InputStream getObject(String bucketName, String objectName, Long offset, Long length) {
        bucketName = getOrDefaultBucket(bucketName);
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .offset(offset)
                    .length(length)
                    .build();
            return getMinioClient().getObject(args).get();
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default boolean buckExist(String bucketName) {
        try {
            return getMinioClient().bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()).get();
        } catch (Exception e) {
            log.error("check buck exist error : {}", e.getMessage());
        }
        return false;
    }

    /**
     * 创建bucket
     *
     * @param bucketName bucket名称
     */
    default void createBucket(String bucketName) {
        if (buckExist(bucketName)) {
            return;
        }
        try {
            MakeBucketArgs args = MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            getMinioClient().makeBucket(args).get();
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default List<BucketItem> listBuckets() {
        try {
            List<Bucket> buckets = getMinioClient().listBuckets().get();
            if (McnUtils.isNotNullAndEmpty(buckets)) {
                return buckets.stream().map(b -> new BucketItem(b.name(), b.creationDate())).collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new MinioException(e);
        }
        return Collections.emptyList();
    }

    /**
     * 删除bucket
     * 该文件夹下不能有文件或子文件夹
     *
     * @param bucketName bucket名称
     */
    default void deleteBucket(String bucketName) {
        try {
            RemoveBucketArgs args = RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            getMinioClient().removeBucket(args).get();
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    default boolean exist(String filename) {
        try {
            getMinioClient().statObject(StatObjectArgs.builder()
                    .bucket(getDefaultBucketName())
                    .object(filename).build()).get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    FileUploadInfo upload(FileUploadInfo fileUploadInfo);

    FileUploadInfo merge(FileUploadInfo fileUploadInfo);

}
