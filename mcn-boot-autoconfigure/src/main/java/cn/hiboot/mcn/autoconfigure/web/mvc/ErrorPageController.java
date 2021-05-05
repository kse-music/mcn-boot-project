package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ErrorPageController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @RequestMapping(ERROR_PATH)
    public RestResp error(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE);
        int code = ErrorMsg.HTTP_ERROR_500;
        if(statusCode == 401){
            code = ErrorMsg.HTTP_ERROR_401;
        }else if(statusCode == 403){
            code = ErrorMsg.HTTP_ERROR_403;
        }else if(statusCode == 404){
            code = ErrorMsg.HTTP_ERROR_404;
        }else if(statusCode == 405){
            code = ErrorMsg.HTTP_ERROR_405;
        }else if(statusCode == 406){
            code = ErrorMsg.HTTP_ERROR_406;
        }else if(statusCode == 408){
            code = ErrorMsg.HTTP_ERROR_408;
        }else if(statusCode == 409){
            code = ErrorMsg.HTTP_ERROR_409;
        }
        return ErrorMsg.buildErrorMessage(code);
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

}