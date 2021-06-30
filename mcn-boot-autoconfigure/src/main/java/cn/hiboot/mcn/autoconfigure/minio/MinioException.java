package cn.hiboot.mcn.autoconfigure.minio;

import cn.hiboot.mcn.core.exception.BaseException;

/**
 * MinioException
 *
 * @author DingHao
 * @since 2021/6/30 14:57
 */
public class MinioException extends BaseException {

    protected MinioException(Integer code) {
        super(code);
    }

    protected MinioException(String msg) {
        super(msg);
    }

    protected MinioException(Integer code, String msg) {
        super(code, msg);
    }

    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinioException(Throwable cause) {
        super(cause);
    }

}
