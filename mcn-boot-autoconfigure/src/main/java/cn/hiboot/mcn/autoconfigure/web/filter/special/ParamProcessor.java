package cn.hiboot.mcn.autoconfigure.web.filter.special;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.core.util.SpringBeanUtils;

/**
 * ParamProcessor
 *
 * @author DingHao
 * @since 2022/6/6 15:11
 */
public interface ParamProcessor extends NameValueProcessor {

    /**
     * 使用CheckParam注解时不受黑白名单限制
     * @param rule 规则
     * @param name 字段名
     * @param value 字段值
     * @return 字段值
     */
    String process(String rule, String name, String value);

    @Override
    default String process(String name, String value) {
        return process(SpringBeanUtils.getBean(ParamProcessorProperties.class).getRule(),name,value);
    }

}
