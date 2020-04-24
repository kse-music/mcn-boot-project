package cn.hiboot.mcn.autoconfigure.converter.provider;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/13 11:07
 */
public interface TypeProvider {

    boolean match(Class<?> sourceType);

    Object convert(Object sourceData, Class<?> targetType);

}
