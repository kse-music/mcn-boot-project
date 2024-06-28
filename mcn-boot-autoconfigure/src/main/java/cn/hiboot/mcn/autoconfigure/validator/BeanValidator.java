package cn.hiboot.mcn.autoconfigure.validator;

import cn.hiboot.mcn.core.util.SpringBeanUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

/**
 * 手动校验
 *
 * @author DingHao
 * @since 2021/6/30 15:40
 */
public abstract class BeanValidator {

    private static Validator validator;

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
        if(validator == null){
            try{
                validator = SpringBeanUtils.getBean(Validator.class);
            }catch (Exception e){
                validator = Validation.buildDefaultValidatorFactory().getValidator();
            }
        }
        return validator;
    }

}

