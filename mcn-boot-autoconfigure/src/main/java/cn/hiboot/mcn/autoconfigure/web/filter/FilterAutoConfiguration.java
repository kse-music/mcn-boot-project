package cn.hiboot.mcn.autoconfigure.web.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
 * register some filter
 *
 * @author DingHao
 * @since 2019/1/9 11:31
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(CorsProperties.class)
public class FilterAutoConfiguration {

    private final CorsProperties corsProperties;

    public FilterAutoConfiguration(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "filter", name = "cross", havingValue = "true")
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource corsConfigurationSource) {
        FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
        filterRegistrationBean.setOrder(corsProperties.getOrder());
        return filterRegistrationBean;
    }

    @Bean
    @ConditionalOnMissingBean(name = "corsConfigurationSource")
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(corsProperties.getAllowCredentials());
        corsConfiguration.addAllowedOrigin(corsProperties.getAllowedOrigin());
        corsConfiguration.addAllowedHeader(corsProperties.getAllowedHeader());
        corsConfiguration.addAllowedMethod(corsProperties.getAllowedMethod());
        corsConfiguration.setMaxAge(corsProperties.getMaxAge());
        source.registerCorsConfiguration(corsProperties.getPattern(), corsConfiguration);
        return source;
    }

    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public DurationAop durationAop() {
        return new DurationAop();
    }

}
