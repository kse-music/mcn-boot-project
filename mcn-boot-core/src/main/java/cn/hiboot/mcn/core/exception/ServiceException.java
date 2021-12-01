package cn.hiboot.mcn.core.exception;

/**
 * ServiceException
 *
 * @author DingHao
 * @since 2021/11/30 13:28
 */
public class ServiceException extends BaseException{
	
	private ServiceException(Integer code) {
		super(code);
	}

	private ServiceException(String msg) {
		super(msg);
	}

	public static ServiceException newInstance(Integer code){
		return new ServiceException(code);
	}

	public static ServiceException newInstance(String msg){
		return new ServiceException(msg);
	}

}
