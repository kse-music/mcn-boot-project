package cn.hiboot.mcn.autoconfigure.jwt;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import org.springframework.security.core.AuthenticationException;

public class InvalidAuthenticationTokenException extends AuthenticationException {

    public InvalidAuthenticationTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidAuthenticationTokenException newInstance(Throwable cause){
        return new InvalidAuthenticationTokenException(ExceptionKeys.AUTHENTICATION_ERROR+"",cause);
    }

}
