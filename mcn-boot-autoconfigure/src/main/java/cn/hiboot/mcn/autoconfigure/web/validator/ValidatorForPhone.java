package cn.hiboot.mcn.autoconfigure.web.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 手机号校验实现
 *
 * @author DingHao
 * @since 2021/6/30 15:45
 */
public class ValidatorForPhone implements ConstraintValidator<Phone, String> {

    public boolean isValid(String value, ConstraintValidatorContext context) {
        //禁用默认提示信息
//        context.disableDefaultConstraintViolation();
        //设置提示语
//        context.buildConstraintViolationWithTemplate("The phone number not correct").addConstraintViolation();
        String pattern = "^\\d{11}$";
        return value != null && Pattern.matches(pattern, value);
    }
}

