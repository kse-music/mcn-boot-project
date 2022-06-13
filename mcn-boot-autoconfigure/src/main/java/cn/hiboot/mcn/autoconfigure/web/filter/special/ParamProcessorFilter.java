package cn.hiboot.mcn.autoconfigure.web.filter.special;

import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessorFilter;

/**
 * ParamProcessorFilter
 *
 * @author DingHao
 * @since 2022/6/9 11:35
 */
public class ParamProcessorFilter extends ValueProcessorFilter {

    public ParamProcessorFilter(ParamProcessorProperties paramProcessorProperties, ParamProcessor paramProcessor) {
        super(paramProcessorProperties, paramProcessor);
    }

}
