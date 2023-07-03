package cn.hiboot.mcn.core.model.result;

import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.model.HttpTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.function.BiFunction;

/**
 * 接口返回数据统一格式
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResp<T> implements HttpTime {

    public enum ActionStatusMethod {

		/**
		 * 成功
		 */
		OK,
		/**
		 * 失败
		 */
		FAIL

    }

	/**
	 * 接口响应状态:OK/FAIL
	 */
    @JsonProperty("ActionStatus")
	private ActionStatusMethod ActionStatus = ActionStatusMethod.OK;

	/**
	 * 接口返回错误码
	 *
	 */
    @JsonProperty("ErrorCode")
	private Integer ErrorCode = 0;

	/**
	 * 接口返回错误信息
	 */
    @JsonProperty("ErrorInfo")
	private String ErrorInfo = "";

	/**
	 * 接口执行时间
	 */
	@JsonProperty("Duration")
	private Long duration;

	/**
	 * 接口返回数据
	 */
	private T data;

	/**
	 * 接口返回数据的总数
	 */
	private Long count;

	public RestResp() {	}

	private RestResp(Integer code,String msg){
		this.ActionStatus = ActionStatusMethod.FAIL;
		this.ErrorCode = code;
		this.ErrorInfo = msg;
	}
	
	public RestResp(T data){
		this.data = data;
	}

    public RestResp(T data, long count) {
        this(data);
        this.count = count;
    }

    @JsonIgnore
	public ActionStatusMethod getActionStatus() {
		return ActionStatus;
	}

	public void setActionStatus(ActionStatusMethod actionStatus) {
		ActionStatus = actionStatus;
	}
	
	@JsonIgnore
	public Integer getErrorCode() {
		return ErrorCode;
	}

	public void setErrorCode(Integer errorCode) {
		ErrorCode = errorCode;
	}
	
	@JsonIgnore
	public String getErrorInfo() {
		return ErrorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		ErrorInfo = errorInfo;
	}

	@JsonIgnore
	@Override
	public Long getDuration() {
		return duration;
	}

	@Override
	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

	public static <S> RestResp<S> error(Integer code,String msg){
		return new RestResp<>(code, msg);
	}

	public static <S> RestResp<S> error(Integer code){
		return error(code, ErrorMsg.getErrorMsg(code));
	}

	public static <S> RestResp<S> error(String msg){
		return error(BaseException.DEFAULT_ERROR_CODE, msg);
	}

	@JsonIgnore
	public boolean isSuccess(){
		return getActionStatus() == ActionStatusMethod.OK;
	}

	@JsonIgnore
	public boolean isFailed(){
		return getActionStatus() == ActionStatusMethod.FAIL;
	}

	public T remoteData(BiFunction<Integer,String,T> errorFunction){
		if(isFailed()){
			if(errorFunction == null){
				throw ServiceException.newInstance(ExceptionKeys.REMOTE_SERVICE_ERROR,getErrorInfo());
			}
			setData(errorFunction.apply(getErrorCode(), getErrorInfo()));
		}
		return getData();
	}

	public T remoteData(){
		return remoteData(null);
	}

}
