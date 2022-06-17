package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessor;
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
    public FilterRegistrationBean<XssFilter> xssFilterRegistration(ValueProcessor xssProcessor) {
        FilterRegistrationBean<XssFilter> filterRegistrationBean = new FilterRegistrationBean<>(new XssFilter(xssProperties,xssProcessor));
        filterRegistrationBean.setOrder(xssProperties.getOrder());
        return filterRegistrationBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public ValueProcessor defaultXssProcessor(){
        return new ValueProcessor(xssProperties) {

            @Override
            public String doProcess(String name, String value) {
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
