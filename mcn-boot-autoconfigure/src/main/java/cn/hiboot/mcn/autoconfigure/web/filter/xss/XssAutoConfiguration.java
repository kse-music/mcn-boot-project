package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorFilter;
import cn.hiboot.mcn.autoconfigure.web.security.WebSecurityProperties;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.util.HtmlUtils;

/**
 * XssAutoConfiguration
 *
 * @author DingHao
 * @since 2022/6/6 10:10
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "mcn.xss", name = "enable", havingValue = "true")
@EnableConfigurationProperties({XssProperties.class, WebSecurityProperties.class})
public class XssAutoConfiguration {

    private final XssProperties xssProperties;

    public XssAutoConfiguration(XssProperties xssProperties) {
        this.xssProperties = xssProperties;
    }

    @Bean
    public FilterRegistrationBean<NameValueProcessorFilter> xssFilterRegistration(XssProcessor xssProcessor) {
        FilterRegistrationBean<NameValueProcessorFilter> filterRegistrationBean = new FilterRegistrationBean<>(new NameValueProcessorFilter(xssProperties,xssProcessor));
        filterRegistrationBean.setOrder(xssProperties.getOrder());
        filterRegistrationBean.setName(xssProperties.getName());
        return filterRegistrationBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public XssProcessor defaultXssProcessor(){
        return (name, value) ->{
            String rs = HtmlUtils.htmlEscape(value);
            if(value.equals(rs)){
                return rs;
            }
            if(xssProperties.isFailedFast()){
                throw ServiceException.newInstance(ExceptionKeys.SPECIAL_SYMBOL_ERROR);
            }
            return rs;
        };
    }

}
