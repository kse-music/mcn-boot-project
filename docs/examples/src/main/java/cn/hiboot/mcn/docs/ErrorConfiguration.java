package cn.hiboot.mcn.docs;

import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionResolver;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageNotReadableException;

@Configuration(proxyBeanMethods = false)
public class ErrorConfiguration {

    @Bean
    public ExceptionResolver<HttpMessageNotReadableException> parameterExceptionResolver() {
        return ExceptionResolver.serviceExceptionResolver(ExceptionKeys.SPECIAL_SYMBOL_ERROR);
    }

    public static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw ServiceException.newInstance(400001, "名称不能为空");
        }
    }
}
