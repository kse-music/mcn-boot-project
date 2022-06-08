package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ValidationExceptionHandler
 *
 * @author DingHao
 * @since 2022/1/13 22:00
 */
class ValidationExceptionHandler{

    public static boolean support(Throwable exception){
        return exception instanceof ValidationException;
    }

    public static List<ValidationErrorBean> handle(Throwable exception) {
        return handleValidationException((ValidationException) exception);
    }

    private static List<ValidationErrorBean> handleValidationException(ValidationException exception){
        List<ValidationErrorBean> data = null;
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) exception;
            data = cve.getConstraintViolations().stream().map(violation1 ->
                    new ValidationErrorBean(violation1.getMessage(), getViolationPath(violation1), getViolationInvalidValue(violation1.getInvalidValue()))
            ).collect(Collectors.toList());
        }
        return data;
    }

    private static String getViolationInvalidValue(Object invalidValue) {
        if (invalidValue == null) {
            return null;
        } else {
            if (invalidValue.getClass().isArray()) {
                if (invalidValue instanceof Object[]) {
                    return Arrays.toString((Object[]) invalidValue);
                }

                if (invalidValue instanceof boolean[]) {
                    return Arrays.toString((boolean[]) invalidValue);
                }

                if (invalidValue instanceof byte[]) {
                    return Arrays.toString((byte[]) invalidValue);
                }

                if (invalidValue instanceof char[]) {
                    return Arrays.toString((char[]) invalidValue);
                }

                if (invalidValue instanceof double[]) {
                    return Arrays.toString((double[]) invalidValue);
                }

                if (invalidValue instanceof float[]) {
                    return Arrays.toString((float[]) invalidValue);
                }

                if (invalidValue instanceof int[]) {
                    return Arrays.toString((int[]) invalidValue);
                }

                if (invalidValue instanceof long[]) {
                    return Arrays.toString((long[]) invalidValue);
                }

                if (invalidValue instanceof short[]) {
                    return Arrays.toString((short[]) invalidValue);
                }
            }

            return invalidValue.toString();
        }
    }

    private static String getViolationPath(ConstraintViolation violation) {
        String rootBeanName = violation.getRootBean().getClass().getSimpleName();
        String propertyPath = violation.getPropertyPath().toString();
        return rootBeanName + (!"".equals(propertyPath) ? '.' + propertyPath : "");
    }

}