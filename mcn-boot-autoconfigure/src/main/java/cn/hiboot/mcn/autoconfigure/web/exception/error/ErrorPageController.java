package cn.hiboot.mcn.autoconfigure.web.exception.error;

import cn.hiboot.mcn.autoconfigure.web.exception.handler.ExceptionHandler;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorPageController extends BasicErrorController {

    @Value("${http.error.override:true}")
    private boolean overrideHttpError;

    public ErrorPageController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        ResponseEntity<Map<String, Object>> error = super.error(request);
        if(overrideHttpError){
            int code = ExceptionKeys.mappingCode(error.getStatusCode().value());
            return restRespToResponseEntity(RestResp.error(code), HttpStatus.valueOf(HttpServletResponse.SC_OK));
        }
        RestResp<Object> resp = (RestResp<Object>) request.getAttribute(ExceptionHandler.EXCEPTION_HANDLE_RESULT_ATTRIBUTE);
        if (resp != null) {
            return restRespToResponseEntity(resp, error.getStatusCode());
        }
        return error;
    }

    private ResponseEntity<Map<String, Object>> restRespToResponseEntity(RestResp<Object> resp, HttpStatusCode status) {
        Map<String, Object> rs = new HashMap<>();
        rs.put("ActionStatus",resp.getActionStatus());
        rs.put("ErrorCode",resp.getErrorCode());
        rs.put("ErrorInfo",resp.getErrorInfo());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json;charset=UTF-8"));
        return new ResponseEntity<>(rs, headers, status);
    }

}