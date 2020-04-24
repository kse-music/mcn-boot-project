package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.web.exception.handler.AbstractExceptionHandler;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.model.ValidationErrorBean;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(Exception.class)
    public RestResp handleException(HttpServletRequest request, Exception exception){
        dealStackTraceElement(exception);
        Object data = null;
        int errorCode = SERVICE_ERROR;
        if(exception instanceof MethodArgumentTypeMismatchException){
            errorCode = PARAM_PARSE_ERROR;
        }else if(exception instanceof MethodArgumentNotValidException){
            errorCode = PARAM_PARSE_ERROR;
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) exception;
            data = dealBindingResult(ex.getBindingResult());
        }else if(exception instanceof BindException){
            errorCode = PARAM_PARSE_ERROR;
            BindException ex = (BindException) exception;
            data = dealBindingResult(ex.getBindingResult());
        }
        logger.error("ErrorMsg = {}",getErrorMsg(errorCode),exception);
        RestResp restResp = buildErrorMessage(errorCode);
        if(data != null){
            restResp.setData(data);
        }
        return restResp;
    }

    private Object dealBindingResult(BindingResult bindingResult){
        return bindingResult.getAllErrors().stream().map((e) -> {
            if(e instanceof FieldError){
                return new ValidationErrorBean(e.getDefaultMessage(),((FieldError )e).getField(), ((FieldError )e).getRejectedValue()==null?null:((FieldError )e).getRejectedValue().toString());
            }
            return new ValidationErrorBean(e.getDefaultMessage(),e.getObjectName(), null);
           }
        ).collect(Collectors.toList());

    }

    @ExceptionHandler(ServletException.class)
    public RestResp handleServletException(HttpServletRequest request, ServletException exception){
        dealStackTraceElement(exception);
        Integer code = HTTP_ERROR_500;
        if (exception instanceof NoHandlerFoundException) {
            code = HTTP_ERROR_404;
        } else if (exception instanceof HttpRequestMethodNotSupportedException) {
            code = HTTP_ERROR_405;
        } else if (exception instanceof HttpMediaTypeException) {
            code = HTTP_ERROR_406;
        } else if (exception instanceof UnavailableException) {
        }
        String errMsg = getErrorMsg(code);
        logger.error("ErrorMsg = {}",errMsg,exception);
        return buildErrorMessage(code);
    }

    @ExceptionHandler(BaseException.class)
    public RestResp handleBaseException(HttpServletRequest request, BaseException exception){
        dealStackTraceElement(exception);
        Integer errorCode = exception.getCode();
        String errMsg = exception.getMsg();
        dealStackTraceElement(exception);
        logger.error("ErrorMsg = {}",errMsg,exception);
        return buildErrorMessage(errorCode,errMsg);
    }

    @ExceptionHandler(ValidationException.class)
    public RestResp handleValidationException(HttpServletRequest request, ValidationException exception){
        dealStackTraceElement(exception);
        RestResp<List<ValidationErrorBean>> objectRestResp = buildErrorMessage(PARAM_PARSE_ERROR);
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) exception;
            objectRestResp.setData(cve.getConstraintViolations().stream().map(violation1 ->
                new ValidationErrorBean(violation1.getMessage(), getViolationPath(violation1), getViolationInvalidValue(violation1.getInvalidValue()))
            ).collect(Collectors.toList()));
        }
        logger.error("ErrorMsg = {}",objectRestResp.getErrorInfo(),exception);
        return objectRestResp;
    }

    private String getViolationInvalidValue(Object invalidValue) {
        if (invalidValue == null) {
            return null;
        } else {
            if (invalidValue.getClass().isArray()) {
                if (invalidValue instanceof Object[]) {
                    return Arrays.toString((Object[])((Object[])invalidValue));
                }

                if (invalidValue instanceof boolean[]) {
                    return Arrays.toString((boolean[])((boolean[])invalidValue));
                }

                if (invalidValue instanceof byte[]) {
                    return Arrays.toString((byte[])((byte[])invalidValue));
                }

                if (invalidValue instanceof char[]) {
                    return Arrays.toString((char[])((char[])invalidValue));
                }

                if (invalidValue instanceof double[]) {
                    return Arrays.toString((double[])((double[])invalidValue));
                }

                if (invalidValue instanceof float[]) {
                    return Arrays.toString((float[])((float[])invalidValue));
                }

                if (invalidValue instanceof int[]) {
                    return Arrays.toString((int[])((int[])invalidValue));
                }

                if (invalidValue instanceof long[]) {
                    return Arrays.toString((long[])((long[])invalidValue));
                }

                if (invalidValue instanceof short[]) {
                    return Arrays.toString((short[])((short[])invalidValue));
                }
            }

            return invalidValue.toString();
        }
    }

    private String getViolationPath(ConstraintViolation violation) {
        String rootBeanName = violation.getRootBean().getClass().getSimpleName();
        String propertyPath = violation.getPropertyPath().toString();
        return rootBeanName + (!"".equals(propertyPath) ? '.' + propertyPath : "");
    }

}
