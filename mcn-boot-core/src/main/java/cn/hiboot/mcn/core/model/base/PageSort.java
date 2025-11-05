package cn.hiboot.mcn.core.model.base;

import cn.hiboot.mcn.core.util.McnAssert;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * PageSort
 *
 * @author DingHao
 * @since 2021/2/8 17:45
 */
@Schema(description = "分页排序参数")
@JsonIgnoreProperties({"offset", "pageIndex", "skip"})
public class PageSort {

    @Schema(description = "当前页码", defaultValue = "1")
    private int pageNo = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private int pageSize = 10;

    @Schema(description = "排序字段集合")
    private List<FieldSort> sort = new ArrayList<>(1);

    public PageSort(){}

    public PageSort(int pageNo, int pageSize) {
        setPageNo(pageNo);
        setPageSize(pageSize);
    }

    public PageSort(List<FieldSort> sort) {
        this.sort = sort;
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getPageIndex() {
        return pageNo - 1;
    }

    public int getSkip() {
        return getOffset();
    }

    public int getOffset() {
        return getPageIndex() * getPageSize();
    }

    public void setPageNo(int pageNo) {
        McnAssert.state(pageNo > 0,"pageNo must gt 0");
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        McnAssert.state(pageSize > 0,"pageSize must gt 0");
        this.pageSize = pageSize;
    }

    public List<FieldSort> getSort() {
        return sort;
    }

    public void setSort(List<FieldSort> sort) {
        McnAssert.notNull(sort,"sort must not be null!");
        this.sort = sort;
    }

}
