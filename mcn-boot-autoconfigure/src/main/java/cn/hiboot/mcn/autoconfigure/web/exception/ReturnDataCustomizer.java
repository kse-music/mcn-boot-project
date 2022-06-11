package cn.hiboot.mcn.autoconfigure.web.exception;

/**
 * ReturnDataCustomizer
 *
 * @author DingHao
 * @since 2022/6/8 14:24
 */
public interface ReturnDataCustomizer<T> {

    T apply(int errorCode,String errorInfo);

}
