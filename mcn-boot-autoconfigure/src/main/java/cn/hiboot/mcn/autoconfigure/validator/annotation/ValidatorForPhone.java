package cn.hiboot.mcn.autoconfigure.validator.annotation;

import cn.hiboot.mcn.core.util.McnUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 手机号校验实现
 *
 * @author DingHao
 * @since 2021/6/30 15:45
 */
public class ValidatorForPhone implements ConstraintValidator<Phone, String> {

    private final Pattern pattern = Pattern.compile("^\\d{11}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(McnUtils.isNullOrEmpty(value)){
            return true;
        }
        return pattern.matcher(value).find();
    }

}

