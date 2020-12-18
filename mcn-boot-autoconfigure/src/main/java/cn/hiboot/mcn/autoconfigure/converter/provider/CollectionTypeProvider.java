package cn.hiboot.mcn.autoconfigure.converter.provider;

import cn.hiboot.mcn.autoconfigure.converter.BeanConversionService;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/13 11:09
 */
public class CollectionTypeProvider implements TypeProvider {

    private final BeanConversionService beanConversionService;

    public CollectionTypeProvider(BeanConversionService beanConversionService) {
        this.beanConversionService = beanConversionService;
    }

    @Override
    public boolean match(Class<?> sourceType) {
        return ClassUtils.isAssignable(Collection.class,sourceType);
    }

    @Override
    public Object convert(Object sourceData, Class<?> targetType) {
        Collection list = (Collection)sourceData;
        Collection rs = new ArrayList<>();
        if(list.size() > 0){
            for (Object o : list) {
                rs.add(beanConversionService.convert(o,targetType));
            }
        }
        return rs;
    }

}
