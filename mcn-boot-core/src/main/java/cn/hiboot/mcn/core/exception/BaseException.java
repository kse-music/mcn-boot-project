package cn.hiboot.mcn.core.exception;



/**
 * 异常基类，各个模块的运行期异常均继承与该类 
 */
public class BaseException extends RuntimeException {

    private Integer code;
    private String msg;

    protected BaseException(Integer code) {
        this(code, null);
    }

    protected BaseException(Integer code, String msg) {
        super(msg(code, msg));
        this.code = code;
        this.msg = getMessage();
    }

    public BaseException(String message) {
        super(message);
        this.code = 999999;
        this.msg = message;
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private static String msg(Integer code, String msg){
        return msg == null ? ErrorMsg.getErrorMsg(code) : ErrorMsg.getErrorMsg(code) + msg;
    }

}