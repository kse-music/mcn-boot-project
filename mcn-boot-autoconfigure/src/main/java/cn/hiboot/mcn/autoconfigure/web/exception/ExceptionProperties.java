package cn.hiboot.mcn.autoconfigure.web.exception;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * ExceptionProperties
 *
 * @author DingHao
 * @since 2022/6/7 9:44
 */
@ConfigurationProperties("mcn.exception.handler")
public class ExceptionProperties {
    /**
     * 是否移除非应用包名的异常栈
     */
    private boolean removeFrameworkStack = true;

    /**
     * 是否返回参数校验失败明细
     */
    private boolean returnValidateResult = false;
    /**
     * 是否将参数校验结果设置到ErrorInfo
     */
    private boolean validateResultToErrorInfo = true;
    /**
     * 是否将校验字段名附在消息上
     */
    private boolean appendField = false;
    /**
     * 指定全局异常处理的顺序
     */
    private int order = Integer.MAX_VALUE;

    /**
     * 是否返回原始异常消息
     */
    private boolean returnOriginExMsg = true;
    /**
     * 是否允许重写内部异常消息
     */
    private boolean overrideExMsg = false;
    /**
     * 是否打印异常栈信息
     */
    private boolean logExMsg = true;


    private Map<Integer,String> errorCodeMsg;

    private JvmError jvmError;

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

    public boolean isValidateResultToErrorInfo() {
        return validateResultToErrorInfo;
    }

    public void setValidateResultToErrorInfo(boolean validateResultToErrorInfo) {
        this.validateResultToErrorInfo = validateResultToErrorInfo;
    }

    public boolean isAppendField() {
        return appendField;
    }

    public void setAppendField(boolean appendField) {
        this.appendField = appendField;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isReturnOriginExMsg() {
        return returnOriginExMsg;
    }

    public void setReturnOriginExMsg(boolean returnOriginExMsg) {
        this.returnOriginExMsg = returnOriginExMsg;
    }

    public boolean isOverrideExMsg() {
        return overrideExMsg;
    }

    public void setOverrideExMsg(boolean overrideExMsg) {
        this.overrideExMsg = overrideExMsg;
    }

    public boolean isLogExMsg() {
        return logExMsg;
    }

    public void setLogExMsg(boolean logExMsg) {
        this.logExMsg = logExMsg;
    }

    public Map<Integer, String> getErrorCodeMsg() {
        return errorCodeMsg;
    }

    public void setErrorCodeMsg(Map<Integer, String> errorCodeMsg) {
        this.errorCodeMsg = errorCodeMsg;
    }

    public JvmError getJvmError() {
        return jvmError;
    }

    public void setJvmError(JvmError jvmError) {
        this.jvmError = jvmError;
    }

    public static class JvmError{
        /**
         * 发生VirtualMachineError错误是否退出JVM
         */
        private boolean exit;
        /**
         * exit status
         * <pre>
         * Runtime.getRuntime().exit(n)
         * </pre>
         */
        private int status = 1;

        public boolean isExit() {
            return exit;
        }

        public void setExit(boolean exit) {
            this.exit = exit;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
