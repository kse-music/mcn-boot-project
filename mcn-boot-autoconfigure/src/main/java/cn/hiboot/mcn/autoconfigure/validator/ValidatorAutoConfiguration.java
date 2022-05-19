package cn.hiboot.mcn.autoconfigure.validator;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/7/27 22:00
 */
@AutoConfiguration(before = ValidationAutoConfiguration.class)
@ConditionalOnClass(ExecutableValidator.class)
@ConditionalOnResource(resources = "classpath:META-INF/services/javax.validation.spi.ValidationProvider")
public class ValidatorAutoConfiguration {

    private final ObjectProvider<LocalValidatorCustomizer> customizers;

    public ValidatorAutoConfiguration(ObjectProvider<LocalValidatorCustomizer> customizers) {
        this.customizers = customizers;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(Validator.class)
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory();
        factoryBean.setMessageInterpolator(interpolatorFactory.getObject());
        factoryBean.getValidationPropertyMap().put("hibernate.validator.fail_fast", "true");
        for (LocalValidatorCustomizer localValidatorCustomizer : customizers) {
            localValidatorCustomizer.customize(factoryBean);
        }
        return factoryBean;
    }

}
