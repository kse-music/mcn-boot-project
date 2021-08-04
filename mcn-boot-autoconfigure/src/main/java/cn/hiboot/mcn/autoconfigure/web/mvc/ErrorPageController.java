package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ErrorPageController implements ErrorController {

    @Value("${http.code.override:true}")
    private boolean overrideHttpCode;

    @RequestMapping("${server.error.path:${error.path:/error}}")
    public RestResp<?> error(HttpServletRequest request, HttpServletResponse response) {
        Integer statusCode = (Integer) request.getAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE);
        int code = ErrorMsg.HTTP_ERROR_500;
        if(statusCode == HttpStatus.UNAUTHORIZED.value()){
            code = ErrorMsg.HTTP_ERROR_401;
        }else if(statusCode == HttpStatus.FORBIDDEN.value()){
            code = ErrorMsg.HTTP_ERROR_403;
        }else if(statusCode == HttpStatus.NOT_FOUND.value()){
            code = ErrorMsg.HTTP_ERROR_404;
        }else if(statusCode == HttpStatus.METHOD_NOT_ALLOWED.value()){
            code = ErrorMsg.HTTP_ERROR_405;
        }else if(statusCode == HttpStatus.NOT_ACCEPTABLE.value()){
            code = ErrorMsg.HTTP_ERROR_406;
        }else if(statusCode == HttpStatus.REQUEST_TIMEOUT.value()){
            code = ErrorMsg.HTTP_ERROR_408;
        }else if(statusCode == HttpStatus.CONFLICT.value()){
            code = ErrorMsg.HTTP_ERROR_409;
        }
        if(overrideHttpCode){
            response.setStatus(HttpServletResponse.SC_OK);
        }
        return ErrorMsg.buildErrorMessage(code);
    }

}