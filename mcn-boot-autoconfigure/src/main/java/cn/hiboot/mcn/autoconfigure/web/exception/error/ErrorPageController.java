package cn.hiboot.mcn.autoconfigure.web.exception.error;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
            int code = ExceptionKeys.mappingCode(statusCode);
            RestResp<Object> resp = RestResp.error(code);
            Map<String, Object> rs = new HashMap<>();
            rs.put("ActionStatus",resp.getActionStatus());
            rs.put("ErrorCode",resp.getErrorCode());
            rs.put("ErrorInfo",resp.getErrorInfo());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/json;charset=UTF-8"));
            return new ResponseEntity<>(rs, headers,HttpStatus.valueOf(HttpServletResponse.SC_OK));
        }
        return error;
    }

}