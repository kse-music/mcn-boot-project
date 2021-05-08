package cn.hiboot.mcn.autoconfigure.jwt;

import cn.hiboot.mcn.core.exception.ErrorMsg;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    public JwtAuthenticationEntryPoint(ObjectMapper mapper){
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        HttpStatus status = HttpStatus.FORBIDDEN;
        int code = ExceptionKeys.PERMISSION_NOT_ENOUGH_ERROR;
        if (authException instanceof InvalidAuthenticationTokenException) {
            status = HttpStatus.UNAUTHORIZED;
            code = ExceptionKeys.AUTHENTICATION_ERROR;
        }

//      restResp.setErrorInfo(authException.getMessage());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        mapper.writeValue(response.getWriter(), ErrorMsg.buildErrorMessage(code));
    }
}