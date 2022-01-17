package cn.hiboot.mcn.autoconfigure.web.exception.error;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * GlobalExceptionViewResolver
 *
 * @author DingHao
 * @since 2022/1/17 14:18
 */
public interface GlobalExceptionViewResolver {

    boolean support(HttpServletRequest request);
    ModelAndView view(HttpServletRequest request, Throwable exception);

}
