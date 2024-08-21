package cn.hiboot.mcn.autoconfigure.minio;

import cn.hiboot.mcn.core.util.McnUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * DefaultFileUploadInfoCache
 *
 * @author DingHao
 * @since 2024/7/26 16:32
 */
public class DefaultFileUploadInfoCache implements FileUploadInfoCache {

    private final Map<String, FileUploadInfo> cache = new ConcurrentHashMap<>();

    private final MinioProperties config;

    public DefaultFileUploadInfoCache(MinioProperties config) {
        this.config = config;
    }

    @Override
    public FileUploadInfo get(String filename) {
        return cache.entrySet().stream().filter(entry -> entry.getValue().getFilename().equals(filename)).findFirst().map(Map.Entry::getValue).orElse(null);
    }

    @Override
    public FileUploadInfo get(FileUploadInfo fileUploadInfo) {
        FileUploadInfo f = cache.get(fileUploadInfo.getMd5());
        long diffInMillis = McnUtils.now().getTime() - f.getCreateAt().getTime();
        long diffInSeconds = TimeUnit.SECONDS.toHours(diffInMillis);
        if (McnUtils.isNotNullAndEmpty(f.getUploadUrls()) && diffInSeconds > config.getExpire()) {
            remove(f);
            return null;
        }
        return f;
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
