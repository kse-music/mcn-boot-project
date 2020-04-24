package cn.hiboot.mcn.core.exception;


public class ServiceException extends BaseException{
	
	private ServiceException(Integer code) {
		super(code);
	}

	public static ServiceException newInstance(){
		return newInstance(ExceptionKeys.SERVICE_ERROR);
	}
	
	public static ServiceException newInstance(Integer code){
		return new ServiceException(code);
	}

}
