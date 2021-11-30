package cn.hiboot.mcn.core.exception;


/**
 * JsonException
 *
 * @author DingHao
 * @since 2021/11/30 17:45
 */
public class JsonException extends BaseException{

	private JsonException(String message) {
		super(message);
	}

	private JsonException(String message, Throwable cause) {
		super(message, cause);
	}

	private JsonException(Throwable cause) {
		super(cause);
	}

	public static JsonException newInstance(Throwable cause){
		return new JsonException(cause);
	}

}
