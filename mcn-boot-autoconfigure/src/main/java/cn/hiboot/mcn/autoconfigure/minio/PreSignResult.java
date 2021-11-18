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

    private String uploadId;
    private List<String> uploadUrls;

    public PreSignResult(int count) {
        this.uploadUrls = new ArrayList<>(count);
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public List<String> getUploadUrls() {
        return uploadUrls;
    }

    public void setUploadUrls(List<String> uploadUrls) {
        this.uploadUrls = uploadUrls;
    }
}
