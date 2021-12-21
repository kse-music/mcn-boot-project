package cn.hiboot.mcn.autoconfigure.web.util;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * 手动校验
 *
 * @author DingHao
 * @since 2021/6/30 15:40
 */
public class BeanValidator {

    public static <T> void validate(T object) {
        //获得验证器
        Validator validator = getValidator();
        //执行验证
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(object);
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    private static Validator getValidator(){
        try{
            return SpringBeanUtils.getBean(Validator.class);
        }catch (Exception e){
            return Validation.buildDefaultValidatorFactory().getValidator();
        }
    }

}

