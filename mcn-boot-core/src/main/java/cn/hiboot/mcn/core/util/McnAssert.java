package cn.hiboot.mcn.core.util;

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
}
