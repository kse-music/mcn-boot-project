package cn.hiboot.mcn.autoconfigure.web.filter;

import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * uniform register some filter
 *
 * @author DingHao
 * @since 2019/1/9 11:31
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(FilterProperties.class)
public class FilterAutoConfiguration {

    private final FilterProperties filterProperties;

    public FilterAutoConfiguration(FilterProperties filterProperties) {
        this.filterProperties = filterProperties;
    }

    @Bean
    @ConditionalOnClass(CorsConfigurationSource.class)
    @ConditionalOnProperty(prefix = "filter", name = "cross", havingValue = "true")
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin(CorsConfiguration.ALL);
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        corsConfiguration.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }

    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public DurationAop durationAop() {
        return new DurationAop();
    }

    @Bean
    @ConditionalOnClass(name = "org.jsoup.Jsoup")
    @ConditionalOnProperty(prefix = "mcn.xss",name = "enable",havingValue = "true")
    public FilterRegistrationBean<XssFilter> xssFilter() {
        FilterRegistrationBean<XssFilter> filterRegistrationBean = new FilterRegistrationBean<>(new XssFilter(filterProperties));
        filterRegistrationBean.setOrder(2);
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }

}
