package cn.hiboot.mcn.autoconfigure.context;

import cn.hiboot.mcn.core.service.McnAutowired;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * mainly deal annotation @McnAutowired
 */
public class McnBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        if(bean instanceof AutowiredAnnotationBeanPostProcessor){
            AutowiredAnnotationBeanPostProcessor abf = (AutowiredAnnotationBeanPostProcessor) bean;
            Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(6);
            ReflectionUtils.doWithLocalFields(abf.getClass(),(f) -> {
                if(f.getName().equals("autowiredAnnotationTypes")){
                    ReflectionUtils.makeAccessible(f);
                    autowiredAnnotationTypes.addAll((Set<Class<? extends Annotation>>)f.get(abf));
                }
            });
            if(pvs.contains("mcnAutowired") && pvs instanceof MutablePropertyValues){
                PropertyValue pv = pvs.getPropertyValue("mcnAutowired");
                autowiredAnnotationTypes.add((Class<McnAutowired>)pv.getValue());
                ((MutablePropertyValues)pvs).removePropertyValue(pv);//this property actual not exist ,remove it so that applyPropertyValues throw ex
            }
            abf.setAutowiredAnnotationTypes(autowiredAnnotationTypes);
        }
        return pvs;
    }

}