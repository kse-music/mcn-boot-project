package cn.hiboot.mcn.core.model.base;

import cn.hiboot.mcn.core.util.McnAssert;

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
     */
    private String sort;

    public FieldSort() {
    }

    public FieldSort(String field, String sort) {
        setField(field);
        setSort(sort);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        McnAssert.hasText(field,"field must not be empty");
        this.field = field;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

}
