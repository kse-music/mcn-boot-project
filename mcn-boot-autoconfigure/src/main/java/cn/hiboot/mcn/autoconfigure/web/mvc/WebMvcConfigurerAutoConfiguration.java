package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.core.model.result.RestResp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * WebMvcConfigurerAutoConfiguration
 *
 * @author DingHao
 * @since 2021/5/9 20:46
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcConfigurerAutoConfiguration {


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(ObjectMapper.class)
    private static class WebMvcConfig implements WebMvcConfigurer {

        private final ObjectMapper objectMapper;

        public WebMvcConfig(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new MethodArgumentResolver(objectMapper));
        }

    }

    @SuppressWarnings("all")
    private static class MethodArgumentResolver implements HandlerMethodArgumentResolver{

        private final ObjectMapper objectMapper;

        public MethodArgumentResolver(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(StrToObj.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            String name = Conventions.getVariableNameForParameter(parameter);
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if(request == null){
                return null;
            }
            return readValue(objectMapper,request.getParameter(name),parameter.getParameterType());
        }
    }

    private static Object readValue(ObjectMapper objectMapper,String data,Class<?> clazz){
        if(data == null){
            return null;
        }
        try {
            return objectMapper.readValue(data,clazz);
        } catch (JsonProcessingException e) {
            return data;
        }
    }

    @SuppressWarnings("all")
    @ControllerAdvice
    @Configuration(proxyBeanMethods = false)
    private static class RestRespResponseBodyAdvice implements ResponseBodyAdvice<RestResp> {

        @Autowired
        private ObjectMapper objectMapper;

        @Override
        public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
            return returnType.getParameterType() == RestResp.class && returnType.hasMethodAnnotation(StrToObj.class);
        }

        @Override
        public RestResp beforeBodyWrite(RestResp body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
            if(body != null && body.getData() instanceof String){
                body.setData(readValue(objectMapper,body.getData().toString(),returnType.getMethodAnnotation(StrToObj.class).value()));
            }
            return body;
        }
    }

}
