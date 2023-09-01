package cn.hiboot.mcn.autoconfigure.validator;

import jakarta.validation.executable.ExecutableValidator;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/7/27 22:00
 */
@AutoConfiguration
@ConditionalOnClass(ExecutableValidator.class)
public class ValidatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ValidationConfigurationCustomizer customValidationConfigurationCustomizer(){
        return configuration -> {
            if(configuration instanceof ConfigurationImpl c){
                c.failFast(true);
            }
        };
    }

}
