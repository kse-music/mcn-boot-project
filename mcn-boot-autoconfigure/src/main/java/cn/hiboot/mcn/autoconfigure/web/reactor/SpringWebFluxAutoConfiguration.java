package cn.hiboot.mcn.autoconfigure.web.reactor;

import cn.hiboot.mcn.autoconfigure.web.exception.handler.GlobalExceptionProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.cors.CorsProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * SpringWebFluxAutoConfiguration
 *
 * @author DingHao
 * @since 2022/5/12 11:30
 */
@AutoConfiguration(before = ErrorWebFluxAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class SpringWebFluxAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @Import(GlobalErrorExceptionHandler.class)
    @EnableConfigurationProperties({ ServerProperties.class, WebProperties.class, WebFluxProperties.class, GlobalExceptionProperties.class})
    protected static class ReactiveExceptionHandler{

        @Bean
        @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
        public DefaultErrorAttributes errorAttributes() {
            return new DefaultErrorAttributes();
        }

    }

    @EnableConfigurationProperties({CorsProperties.class})
    @Configuration(proxyBeanMethods = false)
    protected static class FilterAutoConfiguration{

        @Bean
        @ConditionalOnMissingBean
        public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties ) {
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

    }

}
