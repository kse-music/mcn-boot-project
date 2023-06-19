package cn.hiboot.mcn.autoconfigure.web.exception;

import cn.hiboot.mcn.autoconfigure.web.exception.handler.DefaultExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.mvc.SpringMvcAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.web.reactor.SpringWebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * ExceptionHandlerAutoConfiguration
 *
 * @author DingHao
 * @since 2023/6/17 23:27
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({SpringMvcAutoConfiguration.class, SpringWebFluxAutoConfiguration.class})
@Import(DefaultExceptionHandler.class)
@ConditionalOnWebApplication
@EnableConfigurationProperties(ExceptionProperties.class)
public class ExceptionHandlerAutoConfiguration {


}
