package cn.hiboot.mcn.autoconfigure.web.filter.common;

import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssProperties;
import cn.hiboot.mcn.autoconfigure.web.mvc.WebUtils;
import cn.hiboot.mcn.core.util.McnAssert;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DelegateNameValueProcessor
 *
 * @author DingHao
 * @since 2022/7/21 6:40
 */
class DelegateNameValueProcessor implements NameValueProcessor{

    private final List<ValueProcessor> valueProcessors;

    DelegateNameValueProcessor(ObjectProvider<NameValueProcessor> valueProcessors) {
        this.valueProcessors = valueProcessors.orderedStream().map(v -> {
            McnAssert.state(v instanceof ParamProcessor || v instanceof XssProcessor,"NameValueProcessor must be ParamProcessor or XssProcessor impl");
            NameValueProcessorProperties properties;
            if(v instanceof ParamProcessor){
                properties = SpringBeanUtils.getBean(ParamProcessorProperties.class);
            }else {
                properties = SpringBeanUtils.getBean(XssProperties.class);
            }
            return new ValueProcessor(v,properties);
        }).collect(Collectors.toList());
    }

    @Override
    public String process(String name, String value) {
        for (ValueProcessor valueProcessor : valueProcessors) {
            if(valueProcessor.match(name)){
                value = valueProcessor.process(name, value);
            }
        }
        return value;
    }

    private static class ValueProcessor implements NameValueProcessor{

        NameValueProcessor nameValueProcessor;
        RequestMatcher requestMatcher;
        List<String> excludeFields;
        boolean escapeResponse;

        public ValueProcessor(NameValueProcessor nameValueProcessor, NameValueProcessorProperties properties) {
            this.nameValueProcessor = nameValueProcessor;
            this.requestMatcher = new RequestMatcher(properties.getIncludeUrls(), properties.getExcludeUrls()).enableDefaultExclude();
            this.excludeFields = properties.getExcludeFields();
            if(properties instanceof XssProperties xssProperties){
                this.escapeResponse = xssProperties.isEscapeResponse();
            }
        }

        boolean match(String name){
            if(excludeFields != null && excludeFields.contains(name)){
                return false;
            }
            HttpServletRequest httpServletRequest = WebUtils.getHttpServletRequest();
            if(httpServletRequest == null){
                return true;
            }
            return requestMatcher.matches(httpServletRequest);
        }

        @Override
        public String process(String name, String value) {
            if(name == null && !escapeResponse){//serializers
                return value;
            }
            return nameValueProcessor.process(name, value);
        }
    }

}
