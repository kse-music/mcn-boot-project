package cn.hiboot.mcn.autoconfigure.web.validator;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

/**
 * AbstractConstraintValidator
 *
 * @author DingHao
 * @since 2021/7/26 17:36
 */
public abstract class AbstractConstraintValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

    @Override
    public boolean isValid(T value, ConstraintValidatorContext context) {
        config(context);
        return isValid(value);
    }

    protected void config(ConstraintValidatorContext context){

    }

    public abstract boolean isValid(T value);

}
