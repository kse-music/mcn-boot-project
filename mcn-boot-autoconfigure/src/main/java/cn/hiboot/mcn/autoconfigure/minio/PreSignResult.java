package cn.hiboot.mcn.autoconfigure.minio;

import java.util.ArrayList;
import java.util.List;

/**
 * PreSignResult
 *
 * @author DingHao
 * @since 2021/11/10 14:09
 */
public class PreSignResult {

    private final String uploadId;
    private final List<String> uploadUrls;

    public PreSignResult(String uploadId,int size) {
        this.uploadId = uploadId;
        this.uploadUrls = new ArrayList<>(size);
    }

    public String getUploadId() {
        return uploadId;
    }

    public List<String> getUploadUrls() {
        return uploadUrls;
    }
}
