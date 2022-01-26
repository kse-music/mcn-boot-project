package cn.hiboot.mcn.autoconfigure.validator;

import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 手机号校验实现
 *
 * @author DingHao
 * @since 2021/6/30 15:45
 */
public class ValidatorForPhone implements ConstraintValidatorExtend<Phone, String> {

    private final Pattern pattern = Pattern.compile("^\\d{11}$");

    @Override
    public void config(ConstraintValidatorContext context) {
        //禁用默认提示信息
//        context.disableDefaultConstraintViolation();
        //设置提示语
//        context.buildConstraintViolationWithTemplate("The phone number not correct").addConstraintViolation();
    }

    @Override
    public boolean isValid(String value) {
        if(value == null || value.trim().equals("")){
            return true;
        }
        return pattern.matcher(value).find();
    }

}

