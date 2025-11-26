package cn.hiboot.mcn.autoconfigure.web.reactor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.boot.webflux.autoconfigure.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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

}
