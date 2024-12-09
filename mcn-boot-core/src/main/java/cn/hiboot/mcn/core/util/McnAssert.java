package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.exception.ErrorMsg;

import java.util.function.Supplier;

/**
 * McnAssert
 *
 * @author DingHao
 * @since 2022/1/19 14:23
 */
public abstract class McnAssert {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object, Supplier<String> message) {
        if (object == null) {
            throw new IllegalArgumentException(nullSafeGet(message));
        }
    }

    public static void hasText(String text, String message) {
        if (McnUtils.isNullOrEmpty(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasText(String text, Supplier<String> message) {
        if (McnUtils.isNullOrEmpty(text)) {
            throw new IllegalArgumentException(nullSafeGet(message));
        }
    }

    public static void state(boolean state, String message) {
        if (!state) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void state(boolean state, Supplier<String> message) {
        if (!state) {
            throw new IllegalArgumentException(nullSafeGet(message));
        }
    }

    public static void notNull(Object object, Integer errorCode) {
        notNull(object, () -> ErrorMsg.getErrorMsg(errorCode));
    }

    public static void hasText(String text, Integer errorCode) {
        hasText(text, () -> ErrorMsg.getErrorMsg(errorCode));
    }

    public static void state(boolean state, Integer errorCode) {
        state(state, () -> ErrorMsg.getErrorMsg(errorCode));
    }

    private static String nullSafeGet(Supplier<String> messageSupplier) {
        return (messageSupplier != null ? messageSupplier.get() : null);
    }

}
