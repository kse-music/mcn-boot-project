package cn.hiboot.mcn.autoconfigure.web.reactor;

import cn.hiboot.mcn.autoconfigure.web.filter.FilterAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.cors.CorsProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
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
    @EnableConfigurationProperties({ ServerProperties.class, WebProperties.class, WebFluxProperties.class})
    protected static class ReactiveExceptionHandler{

        @Bean
        @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
        public DefaultErrorAttributes errorAttributes() {
            return new DefaultErrorAttributes();
        }

    }

    @EnableConfigurationProperties(CorsProperties.class)
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "filter.cross", name = "enabled", havingValue = "true")
    protected static class CorsAutoConfiguration{

        @Bean
        @ConditionalOnMissingBean
        CorsWebFilter corsWebFilter(CorsConfigurationSource corsConfigurationSource){
            return new CorsWebFilter(corsConfigurationSource);
        }

        @Bean
        @ConditionalOnMissingBean
        public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration(corsProperties.getPattern(), FilterAutoConfiguration.corsConfiguration(corsProperties));
            return source;
        }

    }

}
