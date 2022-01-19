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
        Sort s = Sort.unsorted();
        for (FieldSort fieldSort : sort) {
            if(s.isUnsorted()){
                s = jpaSort(fieldSort);
                continue;
            }
            s = s.and(jpaSort(fieldSort));
        }
        return s;
    }

    public static Sort jpaSort(FieldSort fieldSort) {
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
