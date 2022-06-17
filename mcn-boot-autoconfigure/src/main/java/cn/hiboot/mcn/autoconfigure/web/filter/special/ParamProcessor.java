package cn.hiboot.mcn.autoconfigure.web.filter.special;

import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessor;

/**
 * ParamProcessor
 *
 * @author DingHao
 * @since 2022/6/6 15:11
 */
public abstract class ParamProcessor extends ValueProcessor {

    protected ParamProcessor(ParamProcessorProperties properties) {
        super(properties);
    }

    /**
     * 使用CheckParam注解时不受黑白名单限制
     * @param rule 规则
     * @param name 字段名
     * @param value 字段值
     * @return 字段值
     */
    public abstract String process(String rule, String name, String value);

    @Override
    public String doProcess(String name, String value) {
        //过滤器上拿不到自定义规则
        return process("",name,value);
    }

}
