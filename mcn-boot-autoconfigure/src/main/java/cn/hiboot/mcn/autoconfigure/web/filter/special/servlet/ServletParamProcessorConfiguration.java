package cn.hiboot.mcn.autoconfigure.web.filter.special.servlet;

import cn.hiboot.mcn.autoconfigure.web.filter.common.servlet.NameValueProcessorFilter;
import cn.hiboot.mcn.autoconfigure.web.filter.special.CheckParam;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * ServletParamProcessorConfiguration
 *
 * @author DingHao
 * @since 2023/5/23 12:25
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServletParamProcessorConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "param.processor", name = "use-filter", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<NameValueProcessorFilter> paramProcessorFilterRegistration(ParamProcessor paramProcessor, ParamProcessorProperties properties) {
        FilterRegistrationBean<NameValueProcessorFilter> filterRegistrationBean = new FilterRegistrationBean<>(new NameValueProcessorFilter(properties, paramProcessor));
        filterRegistrationBean.setOrder(properties.getOrder());
        filterRegistrationBean.setName(properties.getName());
        return filterRegistrationBean;
    }

    @Bean
    public WebMvcConfigurer webMvcConfig(ParamProcessor paramProcessor) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new KeyValueArgumentResolver(paramProcessor));
                resolvers.add(new HandlerMethodArgumentResolver() {

                    private final ServletModelAttributeMethodProcessor processor = new ServletModelAttributeMethodProcessor(true);

                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return processor.supportsParameter(parameter) && (parameter.hasParameterAnnotation(CheckParam.class) || parameter.getParameterType().getAnnotation(CheckParam.class) != null);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                        Object returnValue = processor.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
                        if (returnValue == null) {
                            return null;
                        }
                        return ParamProcessorAutoConfiguration.validStringValue(parameter,returnValue,paramProcessor);
                    }
                });
            }
        };

    }

    protected static class KeyValueArgumentResolver implements HandlerMethodArgumentResolver {

        private final ParamProcessor paramProcessor;

        public KeyValueArgumentResolver(ParamProcessor paramProcessor) {
            this.paramProcessor = paramProcessor;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CheckParam.class) && parameter.getParameterType() == String.class;
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (request == null) {
                return null;
            }
            String name = parameter.getParameterName();
            String rule = parameter.getParameterAnnotation(CheckParam.class).value();
            return paramProcessor.process(rule, name, request.getParameter(name));
        }

    }


}