package cn.hiboot.mcn.autoconfigure.converter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/7/13 11:06
 */
@Aspect
public class ReturnValueAop {

    private BeanConversionService beanConversionService;

    public ReturnValueAop(BeanConversionService beanConversionService) {
        this.beanConversionService = beanConversionService;
    }

    @Around("@annotation(cn.hiboot.mcn.autoconfigure.converter.ReturnTypeProvider)")
    public Object obj(ProceedingJoinPoint pjp) throws Throwable {
        Object obj = pjp.proceed();
        Method targetMethod = ((MethodSignature) pjp.getSignature()).getMethod();

        ReturnTypeProvider annotation = targetMethod.getAnnotation(ReturnTypeProvider.class);
        Class<?> value = annotation.value();

        return beanConversionService.convert(obj,value);

    }

}
