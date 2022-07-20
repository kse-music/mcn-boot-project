package cn.hiboot.mcn.autoconfigure.web.filter.common;


/**
 * 参数值处理器
 *
 * @author DingHao
 * @since 2022/6/9 11:50
 */
public interface ValueProcessor {

    String process(String name, String value);

}
