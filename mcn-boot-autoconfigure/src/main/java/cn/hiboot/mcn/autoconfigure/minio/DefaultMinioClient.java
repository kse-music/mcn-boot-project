package cn.hiboot.mcn.autoconfigure.minio;

import cn.hiboot.mcn.core.task.TaskThreadPool;
import cn.hiboot.mcn.core.util.McnUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Part;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * DefaultMinioClient
 *
 * @author DingHao
 * @since 2021/11/8 17:31
 */
public class DefaultMinioClient extends MinioClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultMinioClient.class);

    private long size;

    private static final int MAX_PART = ObjectWriteArgs.MAX_MULTIPART_COUNT;

    private OkHttpClient okHttpClient;

    private TaskThreadPool pool;

    public DefaultMinioClient(MinioProperties minioProperties,MinioClient.Builder builder) {
        this(builder.credentials(minioProperties.getAccessKey(),minioProperties.getSecretKey())
                .endpoint(minioProperties.getEndpoint())
                .httpClient(minioProperties.getClient().okHttpClient())
                .build());
        this.okHttpClient = minioProperties.getClient().okHttpClient();
        this.pool = new TaskThreadPool(minioProperties.getPool().getCore(),minioProperties.getPool().getMax(),minioProperties.getPool().getQueueSize(),minioProperties.getPool().getThreadName());
        this.size = minioProperties.getMinMultipartSize().toBytes();
    }

    private DefaultMinioClient(MinioClient client) {
        super(client);
    }

    public void upload(String bucketName,String objectName,long length,String contentType,InputStream inputStream) throws Exception{
        int intSize = (int)size;
        int count = (int) ((length / size) + 1);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(intSize);
        byte[] d = new byte[intSize];
        PreSignResult preSignResult = preSign(bucketName,objectName,contentType,count);
        int c;
        while ((c = inputStream.read(d)) != -1){
            outputStream.reset();
            outputStream.write(d,0,c);
            byte[] bytes = outputStream.toByteArray();
            String url = preSignResult.uploadUrls.poll();
            pool.execute(() -> upload(url, contentType,bytes));
        }
        pool.closeUntilAllTaskFinish();
        mergeMultipartUpload(bucketName,objectName, preSignResult.uploadId);
    }

    private void upload(String url,String contentType,byte[] data){
        try {
            okHttpClient.newCall(new Request.Builder().url(url).put(RequestBody.create(MediaType.parse(getOrDefault(contentType)),data)).build()).execute();
        } catch (IOException e) {
            log.error("async upload failed {}",e.getMessage());
        }
    }

    private String getOrDefault(String contentType){
        if(McnUtils.isNullOrEmpty(contentType)){
            contentType = "application/octet-stream";
        }
        return contentType;
    }

    private PreSignResult preSign(String bucketName, String objectName,String contentType, int count) throws Exception{
        Multimap<String, String> headers = HashMultimap.create();
        headers.put("Content-Type", getOrDefault(contentType));
        CreateMultipartUploadResponse response = this.createMultipartUpload(bucketName, region, objectName, headers, null);
        String uploadId = response.result().uploadId();

        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("uploadId", uploadId);
        PreSignResult preSignResult = new PreSignResult(uploadId);
        for (int i = 1; i <= count; i++) {
            reqParams.put("partNumber", String.valueOf(i));
            String uploadUrl = getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.DAYS)
                            .extraQueryParams(reqParams)
                            .build());
            preSignResult.uploadUrls.offer(uploadUrl);
        }
        return preSignResult;
    }

    private void mergeMultipartUpload(String bucketName,String objectName, String uploadId) throws Exception{
        Part[] parts = new Part[MAX_PART];
        ListPartsResponse partResult = listParts(bucketName, null, objectName, MAX_PART, 0, uploadId, null, null);
        int partNumber = 1;
        for (Part part : partResult.result().partList()) {
            parts[partNumber - 1] = new Part(partNumber, part.etag());
            partNumber++;
        }
        completeMultipartUpload(bucketName, region, objectName, uploadId, parts, null, null);
    }

    private static class PreSignResult {
        private final String uploadId;
        private final Queue<String> uploadUrls;

        public PreSignResult(String uploadId) {
            this.uploadId = uploadId;
            this.uploadUrls = new ArrayDeque<>();
        }
    }

}
