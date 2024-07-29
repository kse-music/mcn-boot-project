package cn.hiboot.mcn.autoconfigure.minio;

import java.util.List;

/**
 * FileUploadInfo
 *
 * @author DingHao
 * @since 2024/7/26 15:24
 */
public class FileUploadInfo {

    private Integer chunkNum;

    private String md5;

    private String uploadId;

    private String filename;

    private List<String> uploadUrls;

    public Integer getChunkNum() {
        return chunkNum;
    }

    public void setChunkNum(Integer chunkNum) {
        this.chunkNum = chunkNum;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<String> getUploadUrls() {
        return uploadUrls;
    }

    public void setUploadUrls(List<String> uploadUrls) {
        this.uploadUrls = uploadUrls;
    }

}
