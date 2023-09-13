package cn.hiboot.mcn.autoconfigure.web.filter;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorJacksonConfig;
import cn.hiboot.mcn.autoconfigure.web.filter.cors.CorsProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssAutoConfiguration;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.cors.CorsConfiguration;

/**
 * FilterAutoConfiguration
 *
 * @author DingHao
 * @since 2022/8/19 17:59
 */
@AutoConfiguration(before = JacksonAutoConfiguration.class,after = {ParamProcessorAutoConfiguration.class, XssAutoConfiguration.class})
@Import(NameValueProcessorJacksonConfig.class)
@ConditionalOnBean(NameValueProcessor.class)
public class FilterAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public ExceptionResolver<HttpMessageNotReadableException> specialSymbolExceptionResolver() {
        return t -> {
            ServiceException serviceException = ServiceException.find(t);
            if(serviceException == null || serviceException.getCode() != ExceptionKeys.SPECIAL_SYMBOL_ERROR){
                return null;
            }
            return RestResp.error(serviceException.getCode(),serviceException.getMessage());
        };
    }

    public static CorsConfiguration corsConfiguration(CorsProperties corsProperties){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        propertyMapper.from(corsProperties.getAllowCredentials()).to(corsConfiguration::setAllowCredentials);
        propertyMapper.from(corsProperties.getAllowedOrigin()).to(corsConfiguration::addAllowedOrigin);
        propertyMapper.from(corsProperties.getAllowedOrigins()).to(corsConfiguration::setAllowedOrigins);
        propertyMapper.from(corsProperties.getAllowedHeader()).to(corsConfiguration::addAllowedHeader);
        propertyMapper.from(corsProperties.getAllowedHeaders()).to(corsConfiguration::setAllowedHeaders);
        propertyMapper.from(corsProperties.getAllowedMethod()).to(corsConfiguration::addAllowedMethod);
        propertyMapper.from(corsProperties.getAllowedMethods()).to(corsConfiguration::setAllowedMethods);
        propertyMapper.from(corsProperties.getMaxAge()).to(corsConfiguration::setMaxAge);
        propertyMapper.from(corsProperties.getExposedHeaders()).to(corsConfiguration::setExposedHeaders);
        return corsConfiguration;
    }

}
