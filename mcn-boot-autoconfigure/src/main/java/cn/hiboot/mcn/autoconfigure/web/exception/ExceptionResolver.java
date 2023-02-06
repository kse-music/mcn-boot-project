package cn.hiboot.mcn.autoconfigure.web.exception;

import cn.hiboot.mcn.autoconfigure.common.Resolver;
import cn.hiboot.mcn.core.model.result.RestResp;

import javax.servlet.http.HttpServletRequest;

/**
 * 异常解析器
 *
 * @author DingHao
 * @since 2022/6/25 22:26
 */
public interface ExceptionResolver<T extends Throwable> extends Resolver<T> {
    default RestResp<T> resolveException(HttpServletRequest request, T t){
        return resolve(t);
    }
}
