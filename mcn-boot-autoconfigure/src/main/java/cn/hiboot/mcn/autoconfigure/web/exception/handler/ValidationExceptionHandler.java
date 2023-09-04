package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ValidationExceptionHandler
 *
 * @author DingHao
 * @since 2022/1/13 22:00
 */
public class ValidationExceptionHandler{

    public static boolean support(Throwable exception){
        return exception instanceof ValidationException;
    }

    public static List<ValidationErrorBean> handle(Throwable exception) {
        return handleValidationException((ValidationException) exception);
    }

    private static List<ValidationErrorBean> handleValidationException(ValidationException exception){
        List<ValidationErrorBean> data = null;
        if (exception instanceof ConstraintViolationException cve) {
            data = cve.getConstraintViolations().stream().map(violation ->
                    new ValidationErrorBean(violation.getMessage(), getViolationPath(violation), getViolationInvalidValue(violation.getInvalidValue()))
            ).collect(Collectors.toList());
        }
        return data;
    }

    private static String getViolationInvalidValue(Object invalidValue) {
        if (invalidValue == null) {
            return null;
        } else {
            if (invalidValue.getClass().isArray()) {
                if (invalidValue instanceof Object[] value) {
                    return Arrays.toString(value);
                }

                if (invalidValue instanceof boolean[] value) {
                    return Arrays.toString(value);
                }

                if (invalidValue instanceof byte[] value) {
                    return Arrays.toString(value);
                }

                if (invalidValue instanceof char[] value) {
                    return Arrays.toString(value);
                }

                if (invalidValue instanceof double[] value) {
                    return Arrays.toString(value);
                }

                if (invalidValue instanceof float[] value) {
                    return Arrays.toString(value);
                }

                if (invalidValue instanceof int[] value) {
                    return Arrays.toString(value);
                }

                if (invalidValue instanceof long[] value) {
                    return Arrays.toString(value);
                }

                if (invalidValue instanceof short[] value) {
                    return Arrays.toString(value);
                }
            }

            return invalidValue.toString();
        }
    }

    private static String getViolationPath(ConstraintViolation violation) {
        String rootBeanName = violation.getRootBean().getClass().getSimpleName();
        String propertyPath = violation.getPropertyPath().toString();
        if("".equals(propertyPath)){
            return rootBeanName;
        }
        int index = propertyPath.lastIndexOf(".");
        if(index != -1){
            return propertyPath.substring(index + 1);
        }
        return rootBeanName + '.' + propertyPath;
    }

}