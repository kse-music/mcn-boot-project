package cn.hiboot.mcn.core.exception;


public class RestException extends BaseException{
	
    private RestException(Integer code) {
		super(code);
	}

	public static RestException newInstance(){
		return newInstance(ExceptionKeys.PARAM_PARSE_ERROR);
	}
	
	public static RestException newInstance(Integer code){
		return new RestException(code);
	}

}
