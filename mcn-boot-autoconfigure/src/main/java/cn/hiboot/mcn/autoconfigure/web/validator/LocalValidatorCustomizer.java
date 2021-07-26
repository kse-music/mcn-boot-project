package cn.hiboot.mcn.autoconfigure.web.validator;

import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/7/26 23:41
 */
public interface LocalValidatorCustomizer {
    void customize(LocalValidatorFactoryBean localValidatorFactoryBean);
}
