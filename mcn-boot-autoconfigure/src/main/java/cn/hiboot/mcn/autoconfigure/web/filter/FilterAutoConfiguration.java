package cn.hiboot.mcn.autoconfigure.web.filter;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorJacksonConfig;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssAutoConfiguration;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * FilterAutoConfiguration
 *
 * @author DingHao
 * @since 2022/8/19 17:59
 */
@AutoConfiguration(before = JacksonAutoConfiguration.class,after = {ParamProcessorAutoConfiguration.class, XssAutoConfiguration.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(NameValueProcessorJacksonConfig.class)
@ConditionalOnBean(NameValueProcessor.class)
public class FilterAutoConfiguration {

    @Bean
    public ExceptionResolver<HttpMessageNotReadableException> specialSymbolExceptionResolver() {
        return t -> {
            ServiceException serviceException = ServiceException.find(t);
            if(serviceException == null || serviceException.getCode() != ExceptionKeys.SPECIAL_SYMBOL_ERROR){
                return null;
            }
            return RestResp.error(serviceException.getCode(),serviceException.getMessage());
        };
    }

}
