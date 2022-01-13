package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.web.mvc.converter.StrToObj;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.JacksonUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
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
@ConditionalOnClass(WebMvcConfigurer.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcConfigurerAutoConfiguration {


    @Configuration(proxyBeanMethods = false)
    private static class WebMvcConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new StringObjectMethodArgumentResolver());
        }

    }

    private static class StringObjectMethodArgumentResolver implements HandlerMethodArgumentResolver{

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(StrToObj.class) && parameter.getParameterType() != String.class;
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if(request == null){
                return null;
            }
            return JacksonUtils.fromJson(request.getParameter(parameter.getParameterName()),parameter.getParameterType());
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
