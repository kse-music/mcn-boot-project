package cn.hiboot.mcn.autoconfigure.converter.provider;

import cn.hiboot.mcn.autoconfigure.converter.BeanConversionService;
import com.google.common.collect.Lists;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.List;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/13 11:09
 */
public class CollectionTypeProvider implements TypeProvider {

    private BeanConversionService beanConversionService;

    public CollectionTypeProvider(BeanConversionService beanConversionService) {
        this.beanConversionService = beanConversionService;
    }

    @Override
    public boolean match(Class<?> sourceType) {
        return ClassUtils.isAssignable(Collection.class,sourceType);
    }

    @Override
    public Object convert(Object sourceData, Class<?> targetType) {
        List list = (List)sourceData;
        List rs = Lists.newArrayList();
        if(list.size() > 0){
            for (Object o : list) {
                rs.add(beanConversionService.convert(o,targetType));
            }
        }
        return rs;
    }

}
