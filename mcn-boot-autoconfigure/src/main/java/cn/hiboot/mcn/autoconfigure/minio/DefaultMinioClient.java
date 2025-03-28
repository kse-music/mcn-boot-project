package cn.hiboot.mcn.autoconfigure.minio;

import cn.hiboot.mcn.core.task.TaskThreadPool;
import cn.hiboot.mcn.core.util.McnUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.minio.CreateMultipartUploadResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListPartsResponse;
import io.minio.MinioAsyncClient;
import io.minio.ObjectWriteArgs;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DefaultMinioClient
 *
 * @author DingHao
 * @since 2021/11/8 17:31
 */
public class DefaultMinioClient extends MinioAsyncClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultMinioClient.class);

    private long size;

    private static final int MAX_PART = ObjectWriteArgs.MAX_MULTIPART_COUNT;

    private OkHttpClient okHttpClient;

    private TaskThreadPool pool;

    private int expire;

    private Method method;

    private MinioProperties minioProperties;

    public DefaultMinioClient(MinioProperties minioProperties,MinioAsyncClient.Builder builder) {
        this(builder.credentials(minioProperties.getAccessKey(),minioProperties.getSecretKey())
                .endpoint(minioProperties.getEndpoint())
                .httpClient(minioProperties.getClient().okHttpClient())
                .build());
        this.minioProperties = minioProperties;
        this.okHttpClient = minioProperties.getClient().okHttpClient();
        this.pool = TaskThreadPool.builder()
                .corePoolSize(minioProperties.getPool().getCore())
                .maximumPoolSize(minioProperties.getPool().getMax())
                .blockingQueueSize(minioProperties.getPool().getQueueSize())
                .threadNamePrefix(minioProperties.getPool().getThreadName())
                .build();
        this.size = minioProperties.getMinMultipartSize().toBytes();
        this.expire = minioProperties.getExpire();
        this.method = Method.valueOf(minioProperties.getMethod());
    }

    private DefaultMinioClient(MinioAsyncClient client) {
        super(client);
    }

    public MinioProperties getConfig() {
        return minioProperties;
    }

    public void upload(String bucketName, String objectName, long length, String contentType, InputStream inputStream) throws Exception{
        int intSize = (int)size;
        int count = (int) ((length / size) + 1);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(intSize);
        byte[] d = new byte[intSize];
        PreSignResult preSignResult = getPresignedObjectUrl(bucketName,objectName,null,contentType,count);
        int c;
        int index = 0;
        while ((c = inputStream.read(d)) != -1){
            outputStream.reset();
            outputStream.write(d,0,c);
            byte[] bytes = outputStream.toByteArray();
            String url = preSignResult.getUploadUrls().get(index++);
            pool.execute(() -> upload(url, contentType,bytes));
        }
        mergeMultipartUpload(bucketName,objectName, preSignResult.getUploadId());
    }

    private void upload(String url,String contentType,byte[] data){
        try {
            okHttpClient.newCall(new Request.Builder().url(url).put(RequestBody.create(data,MediaType.parse(getOrDefault(contentType)))).build()).execute();
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

    public PreSignResult getPresignedObjectUrl(String bucketName,String objectName,String uploadId,String contentType, int count) throws Exception{
        Multimap<String, String> headers = HashMultimap.create();
        headers.put("Content-Type", getOrDefault(contentType));
        PreSignResult preSignResult = new PreSignResult(count);
        if(count == 1){
            preSignResult.getUploadUrls().add(getPresignedObjectUrl(bucketName,objectName,null));
        }else {
            if (uploadId == null) {
                CreateMultipartUploadResponse response = createMultipartUploadAsync(bucketName, region, objectName, headers, null).get();
                uploadId = response.result().uploadId();
            }
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("uploadId", uploadId);
            preSignResult.setUploadId(uploadId);
            for (int i = 1; i <= count; i++) {
                reqParams.put("partNumber", String.valueOf(i));
                preSignResult.getUploadUrls().add(getPresignedObjectUrl(bucketName,objectName,reqParams));
            }
        }
        List<String> uploadUrls = preSignResult.getUploadUrls();
        if (uploadUrls != null && getConfig().getExternalEndpoint() != null) {
            preSignResult.setUploadUrls(uploadUrls.stream().map(this::replace).collect(Collectors.toList()));
        }
        return preSignResult;
    }

    private String replace(String url) {
        return url.replace(getConfig().getEndpoint(), getConfig().getExternalEndpoint());
    }

    public List<Integer> listParts(String bucketName, String objectName, String uploadId) throws Exception{
        ListPartsResponse partResult = listPartsAsync(bucketName, null, objectName, MAX_PART, 0, uploadId, null, null).get();
        return partResult.result().partList().stream().map(Part::partNumber).collect(Collectors.toList());
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
        ListPartsResponse partResult = listPartsAsync(bucketName, null, objectName, MAX_PART, 0, uploadId, null, null).get();
        int partNumber = 1;
        for (Part part : partResult.result().partList()) {
            parts[partNumber - 1] = new Part(partNumber, part.etag());
            partNumber++;
        }
        if(partNumber > 1){
            completeMultipartUploadAsync(bucketName, region, objectName, uploadId, parts, null, null).get();
        }
    }

}
