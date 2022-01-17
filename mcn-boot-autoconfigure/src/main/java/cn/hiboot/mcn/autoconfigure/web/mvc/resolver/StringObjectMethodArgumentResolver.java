package cn.hiboot.mcn.autoconfigure.web.mvc.resolver;

import cn.hiboot.mcn.core.util.JacksonUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * StringObjectMethodArgumentResolver
 *
 * @author DingHao
 * @since 2022/1/17 10:15
 */
public class StringObjectMethodArgumentResolver implements HandlerMethodArgumentResolver {

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