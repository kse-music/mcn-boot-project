package cn.hiboot.mcn.core.exception;


public class JsonException extends BaseException{
	
    private JsonException(Integer code) {
		super(code);
	}

	public static JsonException newInstance(){
		return newInstance(ExceptionKeys.JSON_PARSE_ERROR);
	}
	
	public static JsonException newInstance(Integer code){
		return new JsonException(code);
	}

}
