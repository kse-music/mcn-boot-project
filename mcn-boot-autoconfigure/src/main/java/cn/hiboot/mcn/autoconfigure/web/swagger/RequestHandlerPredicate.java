package cn.hiboot.mcn.autoconfigure.web.swagger;

import springfox.documentation.RequestHandler;

import java.util.function.Predicate;


/**
 * Docket Customizer
 *
 * @author DingHao
 * @since 2020/2/11 20:54
 */
public interface RequestHandlerPredicate {
    Predicate<RequestHandler> get();
}
