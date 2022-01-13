package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.web.exception.handler.GlobalExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.ValidationExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.mvc.error.DefaultErrorView;
import cn.hiboot.mcn.autoconfigure.web.mvc.error.ErrorPageController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;

import java.util.stream.Collectors;

/**
 * spring mvc config
 *
 * @author DingHao
 * @since 2019/3/27 10:56
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({DispatcherServlet.class})
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SpringMvcAutoConfiguration {


    @Configuration(proxyBeanMethods = false)
    @Import({GlobalExceptionHandler.class, ValidationExceptionHandler.class})
    @EnableConfigurationProperties(ServerProperties.class)
    protected static class SpringMvcExceptionHandler{

        @Bean
        @ConditionalOnMissingBean(ErrorController.class)
        public ErrorPageController errorController(ErrorAttributes errorAttributes, ServerProperties serverProperties, ObjectProvider<ErrorViewResolver> errorViewResolvers) {
            return new ErrorPageController(errorAttributes, serverProperties.getError(),errorViewResolvers.orderedStream().collect(Collectors.toList()));
        }

        @Bean(name = "error")
        @ConditionalOnProperty(prefix = "server.error.whitelabel", name = "enabled", matchIfMissing = true)
        @ConditionalOnMissingBean(name = "error")
        public View defaultErrorView(ServerProperties serverProperties) {
            return new DefaultErrorView(serverProperties);
        }

        @Bean
        @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
        public DefaultErrorAttributes errorAttributes() {
            return new DefaultErrorAttributes();
        }

    }


}
