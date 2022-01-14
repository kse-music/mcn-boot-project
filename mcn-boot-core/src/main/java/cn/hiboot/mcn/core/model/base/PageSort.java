package cn.hiboot.mcn.core.model.base;

import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

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
    private int pageNo = 1;
    /**
     * 每页数，默认10
     */
    private int pageSize = 10;

    /**
     * 字多排序,支持多字段排序
     */
    private List<FieldSort> sort = new ArrayList<>(1);

    public PageSort(){}

    public PageSort(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public PageSort(List<FieldSort> sort) {
        this.sort = sort;
    }

    public int getPageNo() {
        return (pageNo - 1) * pageSize;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<FieldSort> getSort() {
        return sort;
    }

    public void setSort(List<FieldSort> sort) {
        Assert.notNull(sort,"sort must not null");
        this.sort = sort;
    }

    public Sort jpaSort(){
        Sort s = Sort.unsorted();
        for (FieldSort fieldSort : sort) {
            if(s.isUnsorted()){
                s = fieldSort.toJpaSort();
                continue;
            }
            s = s.and(fieldSort.toJpaSort());
        }
        return s;
    }

}
