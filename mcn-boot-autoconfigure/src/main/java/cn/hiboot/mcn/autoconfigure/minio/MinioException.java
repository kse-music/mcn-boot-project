package cn.hiboot.mcn.autoconfigure.minio;

/**
 * MinioException
 *
 * @author DingHao
 * @since 2021/6/30 14:57
 */
public class MinioException extends RuntimeException {

    public MinioException(String message) {
        super(message);
    }

    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinioException(Throwable cause) {
        this("Invoke Minio Exception",cause);
    }

}
