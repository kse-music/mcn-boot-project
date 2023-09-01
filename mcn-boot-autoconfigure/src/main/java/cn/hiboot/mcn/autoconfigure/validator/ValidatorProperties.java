package cn.hiboot.mcn.autoconfigure.validator;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ValidatorProperties
 *
 * @author DingHao
 * @since 2023/9/1 12:22
 */
@ConfigurationProperties("mcn.validator")
public class ValidatorProperties {
    private boolean failFast = true;
    private boolean overridingMethodAlterParameterConstraint;
    private boolean multipleCascadedValidationOnReturnValues;
    private boolean parallelMethodsDefineParameterConstraints;

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public boolean isOverridingMethodAlterParameterConstraint() {
        return overridingMethodAlterParameterConstraint;
    }

    public void setOverridingMethodAlterParameterConstraint(boolean overridingMethodAlterParameterConstraint) {
        this.overridingMethodAlterParameterConstraint = overridingMethodAlterParameterConstraint;
    }

    public boolean isMultipleCascadedValidationOnReturnValues() {
        return multipleCascadedValidationOnReturnValues;
    }

    public void setMultipleCascadedValidationOnReturnValues(boolean multipleCascadedValidationOnReturnValues) {
        this.multipleCascadedValidationOnReturnValues = multipleCascadedValidationOnReturnValues;
    }

    public boolean isParallelMethodsDefineParameterConstraints() {
        return parallelMethodsDefineParameterConstraints;
    }

    public void setParallelMethodsDefineParameterConstraints(boolean parallelMethodsDefineParameterConstraints) {
        this.parallelMethodsDefineParameterConstraints = parallelMethodsDefineParameterConstraints;
    }
}
