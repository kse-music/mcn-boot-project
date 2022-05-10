package cn.hiboot.mcn.core.exception;

/**
 * 异常基类，各个模块的运行期异常均继承与该类
 *
 *  @author DingHao
 *  @since 2021/11/30 22:46
 */
public class BaseException extends RuntimeException {

    public static final int DEFAULT_ERROR_CODE = 999999;

    private Integer code = DEFAULT_ERROR_CODE;

    protected BaseException(Integer code) {
        this(code, ErrorMsg.getErrorMsg(code));
    }

    protected BaseException(String msg) {
        super(msg);
    }

    protected BaseException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    protected BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    protected BaseException(Throwable cause) {
        super(cause);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}