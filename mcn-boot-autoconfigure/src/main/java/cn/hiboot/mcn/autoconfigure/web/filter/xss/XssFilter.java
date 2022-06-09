package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessorFilter;

/**
 * XssFilter
 *
 * @author DingHao
 * @since 2019/1/9 11:11
 */
public class XssFilter extends ValueProcessorFilter {

    public XssFilter(XssProperties xssProperties, ValueProcessor valueProcessor) {
        super(xssProperties.getExcludeUrls(),xssProperties.getExcludeFields(),xssProperties.isFilterParameterName(), valueProcessor);
    }

}
