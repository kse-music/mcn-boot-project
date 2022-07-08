package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import cn.hiboot.mcn.autoconfigure.web.filter.common.RequestMatcher;
import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessorFilter;
import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessorJacksonConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.HtmlUtils;

/**
 * XssAutoConfiguration
 *
 * @author DingHao
 * @since 2022/6/6 10:10
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "mcn.xss", name = "enable", havingValue = "true")
@EnableConfigurationProperties(XssProperties.class)
public class XssAutoConfiguration {

    private final XssProperties xssProperties;

    public XssAutoConfiguration(XssProperties xssProperties) {
        this.xssProperties = xssProperties;
    }

    @Bean
    public FilterRegistrationBean<ValueProcessorFilter> xssFilterRegistration(XssProcessor xssProcessor) {
        FilterRegistrationBean<ValueProcessorFilter> filterRegistrationBean = new FilterRegistrationBean<>(new ValueProcessorFilter(xssProperties,xssProcessor));
        filterRegistrationBean.setOrder(xssProperties.getOrder());
        filterRegistrationBean.setName(xssProperties.getName());
        return filterRegistrationBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public XssProcessor defaultXssProcessor(){
        return new XssProcessor() {

            @Override
            public RequestMatcher requestMatcher() {
                return new RequestMatcher(xssProperties.getIncludeUrls(), xssProperties.getExcludeUrls());
            }

            @Override
            public String process(String name, String value) {
                return HtmlUtils.htmlEscape(value);
            }

        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ValueProcessorJacksonConfig valueProcessorJacksonConfig(ObjectProvider<ValueProcessor> valueProcessors) {
        ValueProcessorJacksonConfig valueProcessorJacksonConfig = new ValueProcessorJacksonConfig(valueProcessors);
        valueProcessorJacksonConfig.setEscapeResponse(xssProperties.isEscapeResponse());
        return valueProcessorJacksonConfig;
    }

}
