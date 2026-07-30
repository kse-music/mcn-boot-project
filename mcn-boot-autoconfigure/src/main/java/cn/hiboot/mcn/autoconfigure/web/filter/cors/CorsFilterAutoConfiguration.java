package cn.hiboot.mcn.autoconfigure.web.filter.cors;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * CorsFilterConfiguration
 *
 * @author DingHao
 * @since 2019/1/9 11:31
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(CorsProperties.class)
@ConditionalOnProperty(prefix = "filter.cross", name = "enabled", havingValue = "true")
public class CorsFilterAutoConfiguration {

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class CorsFilterServletConfiguration {

        @Bean
        public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource corsConfigurationSource,CorsProperties corsProperties) {
            FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
            filterRegistrationBean.setOrder(corsProperties.getOrder());
            filterRegistrationBean.setName(corsProperties.getName());
            return filterRegistrationBean;
        }

        @Bean
        @ConditionalOnMissingBean(name = "corsConfigurationSource")
        public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration(corsProperties.getPattern(), newCorsConfiguration(corsProperties));
            return source;
        }

    }

    private static CorsConfiguration newCorsConfiguration(CorsProperties corsProperties){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        PropertyMapper propertyMapper = PropertyMapper.get();
        propertyMapper.from(corsProperties.getAllowCredentials()).to(corsConfiguration::setAllowCredentials);
        if (corsProperties.getAllowedOriginPattern() != null || corsProperties.getAllowedOriginPatterns() != null){
            propertyMapper.from(corsProperties.getAllowedOriginPattern()).to(corsConfiguration::addAllowedOriginPattern);
            propertyMapper.from(corsProperties.getAllowedOriginPatterns()).to(corsConfiguration::setAllowedOriginPatterns);
        }else {
            propertyMapper.from(corsProperties.getAllowedOrigin()).to(corsConfiguration::addAllowedOrigin);
            propertyMapper.from(corsProperties.getAllowedOrigins()).to(corsConfiguration::setAllowedOrigins);
        }
        propertyMapper.from(corsProperties.getAllowedHeader()).to(corsConfiguration::addAllowedHeader);
        propertyMapper.from(corsProperties.getAllowedHeaders()).to(corsConfiguration::setAllowedHeaders);
        propertyMapper.from(corsProperties.getAllowedMethod()).to(corsConfiguration::addAllowedMethod);
        propertyMapper.from(corsProperties.getAllowedMethods()).to(corsConfiguration::setAllowedMethods);
        propertyMapper.from(corsProperties.getMaxAge()).to(corsConfiguration::setMaxAge);
        propertyMapper.from(corsProperties.getExposedHeader()).to(corsConfiguration::addExposedHeader);
        propertyMapper.from(corsProperties.getExposedHeaders()).to(corsConfiguration::setExposedHeaders);
        return corsConfiguration;
    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class CorsFilterReactiveConfiguration {

        @Bean
        @ConditionalOnMissingBean
        CorsWebFilter corsWebFilter(org.springframework.web.cors.reactive.CorsConfigurationSource corsConfigurationSource){
            return new CorsWebFilter(corsConfigurationSource);
        }

        @Bean
        @ConditionalOnMissingBean
        public org.springframework.web.cors.reactive.CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
            org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration(corsProperties.getPattern(), newCorsConfiguration(corsProperties));
            return source;
        }

    }

}
