package cn.hiboot.mcn.core.model.base;

import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

/**
 * FieldSort
 *
 * @author DingHao
 * @since 2021/2/8 17:53
 */
public class FieldSort {

    public static final String ASC = "asc";
    public static final String DESC = "desc";

    /**
     * 排序字段名称
     */
    private String field;

    /**
     * asc:升序
     * desc:降序
     * note:忽略大小写比较
     */
    private String sort;

    public FieldSort(String field) {
        this(field,DESC);
    }

    public FieldSort(String field, String sort) {
        setField(field);
        setSort(sort);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        Assert.hasText(field,"field must not be empty");
        this.field = field;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        Assert.hasText(sort,"sort must not be empty");
        this.sort = sort;
    }

    public Sort toJpaSort() {
        if (ASC.equalsIgnoreCase(sort)) {
            return Sort.by(field).ascending();
        }
        return Sort.by(field).descending();
    }

}
