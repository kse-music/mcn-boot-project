package cn.hiboot.mcn.autoconfigure.web.filter.common;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 参数值处理器
 *
 * @author DingHao
 * @since 2022/6/9 11:50
 */
public abstract class ValueProcessor {

    private final RequestMatcher requestMatcher;

    protected ValueProcessor(ValueProcessorProperties properties){
        this.requestMatcher = new RequestMatcher(properties);
    }

    String process(HttpServletRequest request,String name, String value){
        if(request == null){
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(requestAttributes != null) {
                request = requestAttributes.getRequest();
            }
        }
        if(request == null || requestMatcher.matches(request)){
            value = doProcess(name, value);
        }
        return value;
    }

    public abstract String doProcess(String name,String value);

}
