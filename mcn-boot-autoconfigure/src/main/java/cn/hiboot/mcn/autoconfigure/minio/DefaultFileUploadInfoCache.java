package cn.hiboot.mcn.autoconfigure.minio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DefaultFileUploadInfoCache
 *
 * @author DingHao
 * @since 2024/7/26 16:32
 */
public class DefaultFileUploadInfoCache implements FileUploadInfoCache {

    private final Map<String, FileUploadInfo> cache = new ConcurrentHashMap<>();

    @Override
    public FileUploadInfo get(String filename) {
        return cache.entrySet().stream().filter(entry -> entry.getValue().getFilename().equals(filename)).findFirst().map(Map.Entry::getValue).orElse(null);
    }

    @Override
    public FileUploadInfo get(FileUploadInfo fileUploadInfo) {
        return cache.get(fileUploadInfo.getMd5());
    }

    @Override
    public void put(FileUploadInfo fileUploadInfo) {
        cache.put(fileUploadInfo.getMd5(), fileUploadInfo);
    }

    @Override
    public void remove(FileUploadInfo fileUploadInfo) {
        cache.remove(fileUploadInfo.getMd5());
    }

}
