package cn.hiboot.mcn.autoconfigure.web.exception;

import cn.hiboot.mcn.core.exception.ExceptionKeys;
import org.springframework.http.HttpStatusCode;

/**
 * HttpStatusCodeResolver
 *
 * @author DingHao
 * @since 2023/6/17 23:02
 */
public interface HttpStatusCodeResolver {

    Integer resolve(Throwable ex);

    default int mappingCode(HttpStatusCode statusCode) {
        return ExceptionKeys.mappingCode(statusCode.value());
    }

}
