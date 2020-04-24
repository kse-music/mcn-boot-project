package cn.hiboot.mcn.autoconfigure.converter;

import org.dozer.DozerBeanMapper;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/22 0:03
 */
@FunctionalInterface
public interface MapperCustomizer {

    void customize(DozerBeanMapper mapper);

}
