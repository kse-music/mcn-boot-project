package cn.hiboot.mcn.autoconfigure.minio;

import cn.hiboot.mcn.core.util.McnUtils;

import java.util.List;

/**
 * Minio 工具类
 *
 * @author DingHao
 * @since 2021/6/28 22:13
 */
public class DefaultMinio implements Minio {

    private final DefaultMinioClient minioClient;
    public final FileUploadInfoCache fileUploadInfoCache;

    public DefaultMinio(DefaultMinioClient minioClient, FileUploadInfoCache fileUploadInfoCache) {
        this.minioClient = minioClient;
        this.fileUploadInfoCache = fileUploadInfoCache;
        String defaultBucketName = minioClient.getConfig().getDefaultBucketName();
        if (McnUtils.isNullOrEmpty(defaultBucketName)) {
            log.warn("It is recommended to set the default bucket name via minio.default-bucket-name");
        } else {
            //自动创建默认bucketName
            createBucket(defaultBucketName);
        }
    }

    @Override
    public DefaultMinioClient getMinioClient() {
        return minioClient;
    }

    @Override
    public FileUploadInfo upload(FileUploadInfo fileUploadInfo) {
        FileUploadInfo infoCache = fileUploadInfoCache.get(fileUploadInfo);
        if (infoCache == null) {
            fileUploadInfo.setFilename(McnUtils.simpleUUID().concat(".").concat(McnUtils.getExtName(fileUploadInfo.getFilename())));
            PreSignResult presignedObjectUrl = presignedObjectUrl(fileUploadInfo.getFilename(), fileUploadInfo.getUploadId(), fileUploadInfo.getChunkNum());
            fileUploadInfo.setUploadId(presignedObjectUrl.getUploadId());
            fileUploadInfo.setUploadUrls(presignedObjectUrl.getUploadUrls());
            fileUploadInfoCache.put(fileUploadInfo);
        } else {
            fileUploadInfo = infoCache;
            if (fileUploadInfo.getUploadUrls() == null) {
                return fileUploadInfo;
            }
        }
        if (fileUploadInfo.getUploadId() != null) {
            List<Integer> uploaded = listParts(fileUploadInfo.getFilename(), fileUploadInfo.getUploadId());
            if (uploaded.size() == fileUploadInfo.getChunkNum()) {
                merge(fileUploadInfo);
            } else if (uploaded.size() < fileUploadInfo.getChunkNum()) {
                List<String> uploadUrls = fileUploadInfo.getUploadUrls();
                if (!uploaded.isEmpty()) {
                    for (int i = 0; i < uploadUrls.size(); i++) {
                        if (uploaded.contains(i + 1)) {
                            uploadUrls.set(i, null);
                        }
                    }
                }
                fileUploadInfo.setUploadUrls(uploadUrls);
            }
        }
        return fileUploadInfo;
    }

    @Override
    public FileUploadInfo merge(FileUploadInfo fileUploadInfo) {
        FileUploadInfo infoCache = fileUploadInfoCache.get(fileUploadInfo);
        if (infoCache == null) {
            return fileUploadInfo;
        }
        infoCache.setUploadUrls(null);
        String uploadId = infoCache.getUploadId();
        if (uploadId == null) {
            return infoCache;
        }
        mergeMultipartUpload(infoCache.getFilename(), uploadId);
        fileUploadInfoCache.put(infoCache);
        return infoCache;
    }

}
