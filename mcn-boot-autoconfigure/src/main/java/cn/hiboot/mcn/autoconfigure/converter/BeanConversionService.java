package cn.hiboot.mcn.autoconfigure.converter;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/13 0:44
 */
public interface BeanConversionService {

    Object convert(Object sourceData, Class<?> targetType);

}
