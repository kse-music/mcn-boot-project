package cn.hiboot.mcn.autoconfigure.web.filter.xss;

/**
 * XssProcessor
 *
 * @author DingHao
 * @since 2022/6/7 9:54
 */
public interface XssProcessor {
    String process(String text);
}
