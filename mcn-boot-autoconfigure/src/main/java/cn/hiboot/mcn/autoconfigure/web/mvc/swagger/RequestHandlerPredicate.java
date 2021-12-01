package cn.hiboot.mcn.autoconfigure.web.mvc.swagger;

import com.google.common.base.Predicate;
import springfox.documentation.RequestHandler;



/**
 * Docket Customizer
 *
 * @author DingHao
 * @since 2020/2/11 20:54
 */
public interface RequestHandlerPredicate {
    Predicate<RequestHandler> get();
}
