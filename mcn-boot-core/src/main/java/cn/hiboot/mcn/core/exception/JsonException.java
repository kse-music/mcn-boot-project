package cn.hiboot.mcn.core.exception;


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
