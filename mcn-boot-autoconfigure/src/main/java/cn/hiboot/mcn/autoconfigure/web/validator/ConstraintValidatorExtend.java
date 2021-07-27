package cn.hiboot.mcn.autoconfigure.web.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

/**
 * ConstraintValidatorExtend
 *
 * @author DingHao
 * @since 2021/7/26 17:36
 */
public interface ConstraintValidatorExtend<A extends Annotation, T> extends ConstraintValidator<A, T> {

    @Override
    default boolean isValid(T value, ConstraintValidatorContext context) {
        config(context);
        return isValid(value);
    }

    default void config(ConstraintValidatorContext context){

    }

    default boolean isValid(T value){
        return true;
    }

}
