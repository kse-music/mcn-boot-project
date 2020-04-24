package cn.hiboot.mcn.autoconfigure.web.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidatorForPhone.class)
public @interface Phone {

    String message() default "手机号不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
