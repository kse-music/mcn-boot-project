package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.exception.ErrorMsg;

/**
 * McnAssert
 *
 * @author DingHao
 * @since 2022/1/19 14:23
 */
public interface McnAssert {

    static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    static void hasText(String text, String message) {
        if (McnUtils.isNullOrEmpty(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    static void state(boolean state, String message) {
        if (!state) {
            throw new IllegalArgumentException(message);
        }
    }

    static void notNull(Object object, Integer errorCode) {
        notNull(object, ErrorMsg.getErrorMsg(errorCode));
    }

    static void hasText(String text, Integer errorCode) {
        hasText(text, ErrorMsg.getErrorMsg(errorCode));
    }

    static void state(boolean state, Integer errorCode) {
        state(state, ErrorMsg.getErrorMsg(errorCode));
    }

}
