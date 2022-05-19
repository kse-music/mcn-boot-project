package cn.hiboot.mcn.autoconfigure.web.flux;

import cn.hiboot.mcn.autoconfigure.web.filter.CorsProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * SpringWebFluxAutoConfiguration
 *
 * @author DingHao
 * @since 2022/5/12 11:30
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class SpringWebFluxAutoConfiguration {

    @EnableConfigurationProperties({CorsProperties.class})
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
