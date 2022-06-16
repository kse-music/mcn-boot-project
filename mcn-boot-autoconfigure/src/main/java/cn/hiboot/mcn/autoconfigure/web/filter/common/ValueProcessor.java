package cn.hiboot.mcn.autoconfigure.web.filter.common;

/**
 * 参数值处理器
 *
 * @author DingHao
 * @since 2022/6/9 11:50
 */
public interface ValueProcessor {

    /**
     *
     * @param name 字段名
     * @param value 字段值
     * @return value
     */
    String process(String name,String value);

}
