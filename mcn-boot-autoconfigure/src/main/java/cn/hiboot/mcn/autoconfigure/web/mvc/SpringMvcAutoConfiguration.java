package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.minio.MinioException;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionPostProcessor;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.autoconfigure.web.exception.error.DefaultErrorView;
import cn.hiboot.mcn.autoconfigure.web.exception.error.ErrorPageController;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.GlobalExceptionHandler;
import cn.hiboot.mcn.autoconfigure.web.exception.handler.GlobalExceptionProperties;
import cn.hiboot.mcn.autoconfigure.web.mvc.resolver.StrToObj;
import cn.hiboot.mcn.autoconfigure.web.mvc.resolver.StringObjectMethodArgumentResolver;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import io.minio.MinioClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * spring mvc config
 *
 * @author DingHao
 * @since 2019/3/27 10:56
 */
@AutoConfiguration(before = ErrorMvcAutoConfiguration.class)
@ConditionalOnClass({DispatcherServlet.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(DurationAop.class)
public class SpringMvcAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @Import(GlobalExceptionHandler.class)
    @EnableConfigurationProperties({ServerProperties.class, GlobalExceptionProperties.class})
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

        @Bean
        @ConditionalOnProperty(prefix = "mcn.exception.handler",name = "override-ex-msg",havingValue = "true")
        public ExceptionPostProcessor<RestResp<Throwable>> internalExceptionMessagePostProcessor() {
            return resp -> {
                System.out.println();
                return resp;
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(MinioClient.class)
    private static class MinioExceptionResolverConfig{

        @Bean
        @ConditionalOnMissingBean(name = "minioExceptionResolver")
        ExceptionResolver<MinioException> minioExceptionResolver(){
            return t -> RestResp.error(ExceptionKeys.INVOKE_MINIO_ERROR,t.getCause().getMessage());
        }

    }

    @Configuration(proxyBeanMethods = false)
    private static class WebMvcConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new StringObjectMethodArgumentResolver());
        }

    }

    @SuppressWarnings("all")
    @ControllerAdvice
    @Configuration(proxyBeanMethods = false)
    private static class RestRespDataResponseBodyAdvice implements ResponseBodyAdvice<RestResp> {

        @Override
        public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
            return returnType.getParameterType() == RestResp.class && returnType.hasMethodAnnotation(StrToObj.class);
        }

        @Override
        public RestResp beforeBodyWrite(RestResp body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
            if(body != null && body.getData() instanceof String){
                body.setData(JacksonUtils.fromJson(body.getData().toString(),returnType.getMethodAnnotation(StrToObj.class).value()));
            }
            return body;
        }
    }

}
