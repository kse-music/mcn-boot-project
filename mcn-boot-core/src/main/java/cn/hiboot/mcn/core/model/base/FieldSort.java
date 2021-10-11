package cn.hiboot.mcn.core.model.base;

/**
 * FieldSort
 *
 * @author DingHao
 * @since 2021/2/8 17:53
 */
public class FieldSort {
    private String field;
    /**
     * asc:升序
     * desc:降序
     */
    private String sort;

    public FieldSort( String field, String sort ) {
        this.field = field;
        this.sort = sort;
    }

    public FieldSort() {
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
