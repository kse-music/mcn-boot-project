package cn.hiboot.mcn.autoconfigure.minio;

import java.util.concurrent.ExecutionException;

/**
 * MinioException
 *
 * @author DingHao
 * @since 2021/6/30 14:57
 */
public class MinioException extends RuntimeException {

    public MinioException(String message) {
        super(message);//swallow underlying ex
    }

    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinioException(Throwable cause) {
        super(extractException(cause));
    }

    private static Throwable extractException(Throwable cause){
        if(cause instanceof ExecutionException){
            return cause.getCause();
        }
        return cause;
    }
}
