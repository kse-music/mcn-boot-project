package cn.hiboot.mcn.autoconfigure.converter;

import cn.hiboot.mcn.autoconfigure.converter.provider.TypeProvider;
import ma.glasnost.orika.MapperFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/13 11:26
 */
public class DefaultBeanConversionService implements BeanConversionService{

    private static final Map<Class<?>, TypeProvider> cache = new ConcurrentHashMap<>();
    private static final Map<Class<?>,Class<?>> convertMapperCache = new ConcurrentHashMap<>();

    private MapperFactory mapperFactory;
    private ObjectProvider<TypeProvider> typeProviders;

    public DefaultBeanConversionService(MapperFactory mapperFactory, ObjectProvider<TypeProvider> typeProviders) {
        this.mapperFactory = mapperFactory;
        this.typeProviders = typeProviders;
    }

    @Override
    public Object convert(Object sourceData, Class<?> targetType) {
        Class<?> sourceDataClass = sourceData.getClass();
        if(convertMapperCache.containsKey(sourceDataClass)){
            return mapperFactory.getMapperFacade().map(sourceData,targetType);
        }
        if(cache.containsKey(sourceDataClass)){
            return cache.get(sourceDataClass).convert(sourceData,targetType);
        }
        Iterator<TypeProvider> iterator = typeProviders.iterator();
        while (iterator.hasNext()){
            TypeProvider next = iterator.next();
            if(next.match(sourceDataClass)){
                cache.put(sourceDataClass,next);
                return next.convert(sourceData,targetType);
            }
        }
        convertMapperCache.put(sourceDataClass,targetType);
        return mapperFactory.getMapperFacade().map(sourceData,targetType);
    }

}
