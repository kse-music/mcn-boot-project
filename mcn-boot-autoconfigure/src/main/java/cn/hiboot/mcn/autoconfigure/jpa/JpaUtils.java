package cn.hiboot.mcn.autoconfigure.jpa;

import cn.hiboot.mcn.core.model.base.FieldSort;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Sort;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;

/**
 * JpaUtils
 *
 * @author DingHao
 * @since 2022/1/19 14:20
 */
public abstract class JpaUtils {

    public static Sort jpaSort(List<FieldSort> sort){
        Sort jpaSort = null;
        for (FieldSort fieldSort : sort) {
            Sort s = jpaSort(fieldSort);
            if(jpaSort == null){
                jpaSort = s;
            }else {
                jpaSort = jpaSort.and(s);
            }
        }
        return jpaSort == null ? Sort.unsorted() : jpaSort;
    }

    public static Sort jpaSort(FieldSort fieldSort) {
        if(fieldSort.getSort() == null){
            return Sort.unsorted();
        }
        if (FieldSort.ASC.equalsIgnoreCase(fieldSort.getSort())) {
            return Sort.by(fieldSort.getField()).ascending();
        }
        return Sort.by(fieldSort.getField()).descending();
    }

    public static void copyTo(Object updateData,Object existData){
        BeanUtils.copyProperties(updateData,existData,getNullPropertyNames(updateData));
    }

    private static String[] getNullPropertyNames(Object source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors()).map(PropertyDescriptor::getName).filter(name -> src.getPropertyValue(name) == null).toArray(String[]::new);
    }

}
