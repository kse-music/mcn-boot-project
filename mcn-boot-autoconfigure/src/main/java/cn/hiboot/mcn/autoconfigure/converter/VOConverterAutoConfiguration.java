package cn.hiboot.mcn.autoconfigure.converter;

import cn.hiboot.mcn.autoconfigure.converter.provider.CollectionTypeProvider;
import cn.hiboot.mcn.autoconfigure.converter.provider.RestRespTypeProvider;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.aspectj.lang.annotation.Aspect;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/13 10:55
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({MapperFactory.class, Aspect.class})
@Import({DefaultBeanConversionService.class, RestRespTypeProvider.class, CollectionTypeProvider.class})
public class VOConverterAutoConfiguration {

    @Bean
    public ReturnValueAop returnValueAop(BeanConversionService beanConversionService) {
        return new ReturnValueAop(beanConversionService);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(DozerBeanMapper.class)
    static class Dozer{

        @Bean
        @ConditionalOnMissingBean(Mapper.class)
        public Mapper mapper(ObjectProvider<MapperCustomizer> customizers) {
            DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
            customizers.orderedStream().forEach((c) -> c.customize(dozerBeanMapper));
            return dozerBeanMapper;
        }

    }

    @Bean
    @ConditionalOnMissingBean(MapperFactory.class)
    public MapperFactory mapperFactory(ObjectProvider<MapperFactoryCustomizer> customizers) {
        DefaultMapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        customizers.orderedStream().forEach((c) -> c.customize(mapperFactory));
        return mapperFactory;
    }

}