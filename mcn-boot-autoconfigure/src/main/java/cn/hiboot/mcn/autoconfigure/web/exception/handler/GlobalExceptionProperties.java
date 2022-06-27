package cn.hiboot.mcn.autoconfigure.web.exception.handler;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GlobalExceptionProperties
 *
 * @author DingHao
 * @since 2022/6/7 9:44
 */
@ConfigurationProperties("mcn.exception.handler")
public class GlobalExceptionProperties {
    /**
     * 是否移除非应用包名的异常栈
     */
    private boolean removeFrameworkStack = true;

    /**
     * 是否返回参数校验失败明细
     */
    private boolean returnValidateResult = false;

    /**
     * 指定全局异常处理的顺序
     */
    private int order = Integer.MAX_VALUE;

    /**
     * 是否返回统一异常消息
     */
    private boolean uniformExMsg;

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
