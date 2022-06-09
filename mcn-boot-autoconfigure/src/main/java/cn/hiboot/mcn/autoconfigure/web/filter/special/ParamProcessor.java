package cn.hiboot.mcn.autoconfigure.web.filter.special;

import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessor;

/**
 * ParamProcessor
 *
 * @author DingHao
 * @since 2022/6/6 15:11
 */
public interface ParamProcessor extends ValueProcessor {

    String process(String rule,String value);

    @Override
    default String process(String value) {
        return process("",value);
    }
}
