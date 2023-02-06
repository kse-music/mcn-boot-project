package cn.hiboot.mcn.autoconfigure.web.exception;

import cn.hiboot.mcn.autoconfigure.common.GenericResolver;

/**
 * GenericExceptionResolver
 *
 * @author DingHao
 * @since 2023/2/6 10:10
 */
public interface GenericExceptionResolver extends GenericResolver<Throwable>,ExceptionResolver<Throwable> {

}
