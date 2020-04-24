package cn.hiboot.mcn.autoconfigure.converter;

import ma.glasnost.orika.MapperFactory;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/21 23:35
 */
@FunctionalInterface
public interface MapperFactoryCustomizer {

    void customize(MapperFactory mapperFactory);

}
