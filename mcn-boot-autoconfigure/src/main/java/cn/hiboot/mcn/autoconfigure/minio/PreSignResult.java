package cn.hiboot.mcn.autoconfigure.minio;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * PreSignResult
 *
 * @author DingHao
 * @since 2021/11/10 14:09
 */
public class PreSignResult {

    private final String uploadId;
    private final Queue<String> uploadUrls;

    public PreSignResult(String uploadId,int size) {
        this.uploadId = uploadId;
        this.uploadUrls = new ArrayDeque<>(size);
    }

    public String getUploadId() {
        return uploadId;
    }

    public Queue<String> getUploadUrls() {
        return uploadUrls;
    }
}
