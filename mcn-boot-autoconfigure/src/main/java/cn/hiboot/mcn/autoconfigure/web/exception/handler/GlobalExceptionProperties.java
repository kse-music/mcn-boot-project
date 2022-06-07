package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GlobalExceptionProperties
 *
 * @author DingHao
 * @since 2022/6/7 9:44
 */
@ConfigurationProperties("global.exception.handler")
public class GlobalExceptionProperties {

    private boolean removeFrameworkStack = true;
    private boolean returnValidateResult = false;
    private int order = 1;
    private boolean uniformExMsg;

    public boolean isRemoveFrameworkStack() {
        return removeFrameworkStack;
    }

    public void setRemoveFrameworkStack(boolean removeFrameworkStack) {
        this.removeFrameworkStack = removeFrameworkStack;
    }

    public boolean isReturnValidateResult() {
        return returnValidateResult;
    }

    public void setReturnValidateResult(boolean returnValidateResult) {
        this.returnValidateResult = returnValidateResult;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isUniformExMsg() {
        return uniformExMsg;
    }

    public void setUniformExMsg(boolean uniformExMsg) {
        this.uniformExMsg = uniformExMsg;
    }
}
