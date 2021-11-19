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
import java.util.HashMap;
import java.util.Map;

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

    private int expire;

    private Method method;

    public DefaultMinioClient(MinioProperties minioProperties,MinioClient.Builder builder) {
        this(builder.credentials(minioProperties.getAccessKey(),minioProperties.getSecretKey())
                .endpoint(minioProperties.getEndpoint())
                .httpClient(minioProperties.getClient().okHttpClient())
                .build());
        this.okHttpClient = minioProperties.getClient().okHttpClient();
        this.pool = new TaskThreadPool(minioProperties.getPool().getCore(),minioProperties.getPool().getMax(),minioProperties.getPool().getQueueSize(),minioProperties.getPool().getThreadName());
        this.size = minioProperties.getMinMultipartSize().toBytes();
        this.expire = minioProperties.getExpire();
        this.method = Method.valueOf(minioProperties.getMethod());
    }

    private DefaultMinioClient(MinioClient client) {
        super(client);
    }

    public void upload(String bucketName,String objectName,long length,String contentType,InputStream inputStream) throws Exception{
        int intSize = (int)size;
        int count = (int) ((length / size) + 1);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(intSize);
        byte[] d = new byte[intSize];
        PreSignResult preSignResult = getPresignedObjectUrl(bucketName,objectName,contentType,count);
        int c;
        int index = 0;
        while ((c = inputStream.read(d)) != -1){
            outputStream.reset();
            outputStream.write(d,0,c);
            byte[] bytes = outputStream.toByteArray();
            String url = preSignResult.getUploadUrls().get(index++);
            pool.execute(() -> upload(url, contentType,bytes));
        }
        pool.closeUntilAllTaskFinish();
        mergeMultipartUpload(bucketName,objectName, preSignResult.getUploadId());
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

    public PreSignResult getPresignedObjectUrl(String bucketName,String objectName,String contentType, int count) throws Exception{
        Multimap<String, String> headers = HashMultimap.create();
        headers.put("Content-Type", getOrDefault(contentType));
        PreSignResult preSignResult = new PreSignResult(count);
        if(count == 1){
            preSignResult.getUploadUrls().add(getPresignedObjectUrl(bucketName,objectName,null));
        }else {
            CreateMultipartUploadResponse response = this.createMultipartUpload(bucketName, region, objectName, headers, null);
            String uploadId = response.result().uploadId();

            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("uploadId", uploadId);
            preSignResult.setUploadId(uploadId);
            for (int i = 1; i <= count; i++) {
                reqParams.put("partNumber", String.valueOf(i));
                preSignResult.getUploadUrls().add(getPresignedObjectUrl(bucketName,objectName,reqParams));
            }
        }
        return preSignResult;
    }

    private String getPresignedObjectUrl(String bucketName,String objectName,Map<String, String> queryParams) throws Exception{
        return getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(method)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expire)
                        .extraQueryParams(queryParams)
                        .build());
    }

    public void mergeMultipartUpload(String bucketName,String objectName, String uploadId) throws Exception{
        Part[] parts = new Part[MAX_PART];
        ListPartsResponse partResult = listParts(bucketName, null, objectName, MAX_PART, 0, uploadId, null, null);
        int partNumber = 1;
        for (Part part : partResult.result().partList()) {
            parts[partNumber - 1] = new Part(partNumber, part.etag());
            partNumber++;
        }
        if(partNumber == 1){
            throw new MinioException("未找到需要合并的块");
        }
        completeMultipartUpload(bucketName, region, objectName, uploadId, parts, null, null);
    }

}
