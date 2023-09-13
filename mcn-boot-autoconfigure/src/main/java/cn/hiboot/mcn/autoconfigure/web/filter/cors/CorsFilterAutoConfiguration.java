package cn.hiboot.mcn.autoconfigure.web.filter.cors;

import cn.hiboot.mcn.autoconfigure.web.filter.FilterAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CorsFilterConfiguration
 *
 * @author DingHao
 * @since 2019/1/9 11:31
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(CorsProperties.class)
@ConditionalOnProperty(prefix = "filter.cross", name = "enabled", havingValue = "true")
public class CorsFilterAutoConfiguration {

    private final CorsProperties corsProperties;

    public CorsFilterAutoConfiguration(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource corsConfigurationSource) {
        FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
        filterRegistrationBean.setOrder(corsProperties.getOrder());
        filterRegistrationBean.setName(corsProperties.getName());
        return filterRegistrationBean;
    }

    @Bean
    @ConditionalOnMissingBean(name = "corsConfigurationSource")
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsProperties.getPattern(), FilterAutoConfiguration.corsConfiguration(corsProperties));
        return source;
    }

}
