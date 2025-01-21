package cn.hiboot.mcn.core.exception;

import java.util.Objects;

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

	private ServiceException(Integer code,String msg) {
		super(code,msg);
	}

	private ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	private ServiceException(Throwable cause) {
		super(cause);
	}

	public static ServiceException newInstance(Throwable cause){
		return new ServiceException(cause);
	}

	public static ServiceException newInstance(String message, Throwable cause){
		return new ServiceException(message,cause);
	}

	public static ServiceException newInstance(Integer code){
		return new ServiceException(code);
	}

	public static ServiceException newInstance(String msg){
		return new ServiceException(msg);
	}

	public static ServiceException newInstance(Integer code,String msg){
		return new ServiceException(code,msg);
	}

	public static ServiceException find(Throwable t){
		if(t instanceof ServiceException serviceException){
			return serviceException;
		}
		Throwable cause = t.getCause();
		if(cause == null){
			return null;
		}
		return find(cause);
	}

	public static ServiceException find(Throwable t, Integer errorCode){
		ServiceException serviceException = find(t);
		if(serviceException == null || !Objects.equals(serviceException.getCode(), errorCode)){
			return null;
		}
		return serviceException;
	}

}
