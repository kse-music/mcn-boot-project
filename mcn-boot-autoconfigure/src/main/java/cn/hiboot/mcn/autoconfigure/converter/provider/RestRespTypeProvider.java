package cn.hiboot.mcn.autoconfigure.converter.provider;

import cn.hiboot.mcn.autoconfigure.converter.BeanConversionService;
import cn.hiboot.mcn.core.model.result.RestResp;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/13 11:09
 */
public class RestRespTypeProvider implements TypeProvider  {

    private BeanConversionService beanConversionService;

    public RestRespTypeProvider(BeanConversionService beanConversionService) {
        this.beanConversionService = beanConversionService;
    }

    @Override
    public boolean match(Class<?> sourceType) {
        return sourceType == RestResp.class;
    }

    @Override
    public Object convert(Object sourceData, Class<?> targetType) {
        return new RestResp<>(beanConversionService.convert(((RestResp)sourceData).getData(), targetType));
    }

}
