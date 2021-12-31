package cn.hiboot.mcn.autoconfigure.web.mvc.error;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorPageController extends BasicErrorController {

    @Value("${http.error.override:true}")
    private boolean overrideHttpError;

    public ErrorPageController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }

    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        ResponseEntity<Map<String, Object>> error = super.error(request);
        if(overrideHttpError){
            int statusCode = error.getStatusCodeValue();
            int code = ExceptionKeys.HTTP_ERROR_500;
            if(statusCode == HttpStatus.UNAUTHORIZED.value()){
                code = ExceptionKeys.HTTP_ERROR_401;
            }else if(statusCode == HttpStatus.FORBIDDEN.value()){
                code = ExceptionKeys.HTTP_ERROR_403;
            }else if(statusCode == HttpStatus.NOT_FOUND.value()){
                code = ExceptionKeys.HTTP_ERROR_404;
            }else if(statusCode == HttpStatus.METHOD_NOT_ALLOWED.value()){
                code = ExceptionKeys.HTTP_ERROR_405;
            }else if(statusCode == HttpStatus.NOT_ACCEPTABLE.value()){
                code = ExceptionKeys.HTTP_ERROR_406;
            }else if(statusCode == HttpStatus.REQUEST_TIMEOUT.value()){
                code = ExceptionKeys.HTTP_ERROR_408;
            }else if(statusCode == HttpStatus.CONFLICT.value()){
                code = ExceptionKeys.HTTP_ERROR_409;
            }
            RestResp<Object> resp = RestResp.error(code);
            Map<String, Object> rs = new HashMap<>();
            rs.put("ActionStatus",resp.getActionStatus());
            rs.put("ErrorCode",resp.getErrorCode());
            rs.put("ErrorInfo",resp.getErrorInfo());
            return new ResponseEntity<>(rs, HttpStatus.valueOf(HttpServletResponse.SC_OK));
        }
        return error;
    }

}