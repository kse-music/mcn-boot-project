package cn.hiboot.mcn.autoconfigure.validator;

import cn.hiboot.mcn.autoconfigure.util.SpringBeanUtils;

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
public abstract class BeanValidator {

    private static volatile Validator validator;

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

