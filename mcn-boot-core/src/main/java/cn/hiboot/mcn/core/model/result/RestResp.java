package cn.hiboot.mcn.core.model.result;

import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ServiceException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.function.BiConsumer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResp<T> {
	
    public enum ActionStatusMethod {
        OK,
        FAIL
    }
	
    @JsonProperty("ActionStatus")
	private ActionStatusMethod ActionStatus = ActionStatusMethod.OK;

    @JsonProperty("ErrorCode")
	private Integer ErrorCode = 0;

    @JsonProperty("ErrorInfo")
	private String ErrorInfo = "";

	@JsonProperty("Duration")
	private Long duration;

	private T data;

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
	public Long getDuration() {
		return duration;
	}

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
	public T feignData(BiConsumer<Integer,String> errorConsumer){
		if(getActionStatus() == ActionStatusMethod.FAIL){
			if(errorConsumer == null){
				throw ServiceException.newInstance(getErrorInfo());
			}
			errorConsumer.accept(getErrorCode(),getErrorInfo());
		}
		return getData();
	}

	@JsonIgnore
	public T feignData(){
		return feignData(null);
	}

}
