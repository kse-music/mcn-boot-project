package cn.hiboot.mcn.autoconfigure.validator;

import jakarta.validation.executable.ExecutableValidator;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.validation.autoconfigure.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/7/27 22:00
 */
@AutoConfiguration
@ConditionalOnClass(ExecutableValidator.class)
@EnableConfigurationProperties(ValidatorProperties.class)
public class ValidatorAutoConfiguration {

    @Bean
    ValidationConfigurationCustomizer customValidationConfigurationCustomizer(ValidatorProperties properties){
        return configuration -> {
            if(configuration instanceof ConfigurationImpl c){
                c.failFast(properties.isFailFast())
                        .allowOverridingMethodAlterParameterConstraint(properties.isOverridingMethodAlterParameterConstraint())
                        .allowMultipleCascadedValidationOnReturnValues(properties.isMultipleCascadedValidationOnReturnValues())
                        .allowParallelMethodsDefineParameterConstraints(properties.isParallelMethodsDefineParameterConstraints());
            }
        };
    }

}
