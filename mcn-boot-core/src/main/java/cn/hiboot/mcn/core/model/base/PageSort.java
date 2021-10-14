package cn.hiboot.mcn.core.model.base;

import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PageSort
 *
 * @author DingHao
 * @since 2021/2/8 17:45
 */
public class PageSort {

    /**
     * 当前页，默认1
     */
    private Integer pageNo = 1;
    /**
     * 每页数，默认10
     */
    private Integer pageSize = 10;

    /**
     * 字多排序,支持多字段排序
     */
    private List<FieldSort> sort = new ArrayList<>(1);

    public PageSort(){}

    public PageSort(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public Integer getPageNo() {
        if(Objects.isNull(pageNo)){//此处不能用三目表达式，猜测利用反射获取的值不能带逻辑判断？
            return pageNo;
        }
        return (pageNo - 1) * pageSize;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<FieldSort> getSort() {
        return sort;
    }

    public void setSort(List<FieldSort> sort) {
        this.sort = sort;
    }

    public List<Sort.Order> getJpaSort(){
        List<Sort.Order> sorts = new ArrayList<>();
        if(ObjectUtils.isEmpty(sort)){
            return sorts;
        }
        for (FieldSort fieldSort : sort) {
            String sort = fieldSort.getSort();
            if (sort.equalsIgnoreCase("desc")) {
                sorts.add(Sort.Order.desc( fieldSort.getField()) );
            } else if (sort.equalsIgnoreCase("asc" )) {
                sorts.add(Sort.Order.asc( fieldSort.getField()) );
            }
        }
        return sorts;
    }

}
