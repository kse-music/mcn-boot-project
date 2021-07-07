package cn.hiboot.mcn.core.exception;


public class JsonException extends BaseException{
	
    private JsonException(Integer code) {
		super(code);
	}

    private JsonException(Throwable cause) {
		super(cause);
	}

	public static JsonException newInstance(){
		return newInstance(ExceptionKeys.JSON_PARSE_ERROR);
	}
	
	public static JsonException newInstance(Integer code){
		return new JsonException(code);
	}

	public static JsonException newInstance(Throwable cause){
		return new JsonException(cause);
	}

}
