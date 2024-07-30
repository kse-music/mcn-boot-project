package cn.hiboot.mcn.autoconfigure.minio;

/**
 * FileUploadInfoCache
 *
 * @author DingHao
 * @since 2024/7/26 16:30
 */
public interface FileUploadInfoCache {

    FileUploadInfo get(String filename);

    FileUploadInfo get(FileUploadInfo fileUploadInfo);

    void put(FileUploadInfo fileUploadInfo);

    void remove(FileUploadInfo fileUploadInfo);

}
