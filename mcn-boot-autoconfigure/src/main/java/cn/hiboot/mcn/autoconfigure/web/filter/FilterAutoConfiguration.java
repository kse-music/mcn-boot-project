package cn.hiboot.mcn.autoconfigure.web.filter;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorJacksonConfig;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssAutoConfiguration;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ServerWebInputException;

/**
 * FilterAutoConfiguration
 *
 * @author DingHao
 * @since 2022/8/19 17:59
 */
@AutoConfiguration(before = JacksonAutoConfiguration.class, after = {ParamProcessorAutoConfiguration.class, XssAutoConfiguration.class})
@Import(NameValueProcessorJacksonConfig.class)
@ConditionalOnBean(NameValueProcessor.class)
public class FilterAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public ExceptionResolver<HttpMessageNotReadableException> specialSymbolExceptionResolverServlet() {
        return specialSymbolExceptionResolver();
    }

    public <E extends Throwable> ExceptionResolver<E> specialSymbolExceptionResolver() {
        return ExceptionResolver.serviceExceptionResolver(ExceptionKeys.SPECIAL_SYMBOL_ERROR);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public ExceptionResolver<ServerWebInputException> specialSymbolExceptionResolverReactive() {
        return specialSymbolExceptionResolver();
    }

}
