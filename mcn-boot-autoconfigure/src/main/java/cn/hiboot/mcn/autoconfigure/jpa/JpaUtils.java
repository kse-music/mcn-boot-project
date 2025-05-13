package cn.hiboot.mcn.autoconfigure.jpa;

import cn.hiboot.mcn.core.model.base.FieldSort;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Sort;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JpaUtils
 *
 * @author DingHao
 * @since 2022/1/19 14:20
 */
public abstract class JpaUtils {

    private static final Map<Class<?>, Object> CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Class<?>> CLASS_UNWRAP_CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    static <T> T getRepository(Class<?> clazz) {
        return (T) CACHE.computeIfAbsent(resolveServiceClass(clazz), serviceClass -> {
            Class<?> serviceInterface = Arrays.stream(serviceClass.getInterfaces())
                    .filter(BaseService.class::isAssignableFrom)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No BaseService interface found in " + serviceClass.getName()));
            ParameterizedType parameterized = Arrays.stream(serviceInterface.getGenericInterfaces())
                    .filter(t -> t instanceof ParameterizedType)
                    .map(t -> (ParameterizedType) t)
                    .filter(pt -> pt.getRawType().getTypeName().startsWith(BaseService.class.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No parameterized BaseService found in " + serviceInterface.getName()));
            Type[] typeArgs = parameterized.getActualTypeArguments();
            if (typeArgs.length != 3 || !(typeArgs[2] instanceof Class<?> repoClass)) {
                throw new IllegalStateException("Cannot resolve repository type in " + serviceInterface.getName());
            }
            return SpringBeanUtils.getBean(repoClass);
        });
    }

    private static Class<?> resolveServiceClass(Class<?> clazz) {
        return CLASS_UNWRAP_CACHE.computeIfAbsent(clazz, c -> {
            while (c != null && c.getName().contains("$$")) {
                c = c.getSuperclass();
            }
            return c;
        });
    }

    public static Sort jpaSort(List<FieldSort> sort) {
        Sort jpaSort = null;
        for (FieldSort fieldSort : sort) {
            Sort s = jpaSort(fieldSort);
            if (s == null) {
                continue;
            }
            if (jpaSort == null) {
                jpaSort = s;
            } else {
                jpaSort = jpaSort.and(s);
            }
        }
        return jpaSort == null ? Sort.unsorted() : jpaSort;
    }

    public static Sort jpaSort(FieldSort fieldSort) {
        if (fieldSort.getSort() == null) {
            return Sort.unsorted();
        }
        if (FieldSort.ASC.equalsIgnoreCase(fieldSort.getSort())) {
            return Sort.by(fieldSort.getField()).ascending();
        }
        return Sort.by(fieldSort.getField()).descending();
    }

    public static void copyTo(Object updateData, Object existData) {
        BeanUtils.copyProperties(updateData, existData, getNullPropertyNames(updateData));
    }

    private static String[] getNullPropertyNames(Object source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors()).map(PropertyDescriptor::getName).filter(name -> src.getPropertyValue(name) == null).toArray(String[]::new);
    }

}
