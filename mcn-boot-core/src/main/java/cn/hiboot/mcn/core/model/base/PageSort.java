package cn.hiboot.mcn.core.model.base;

import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * PageSort
 *
 * @author DingHao
 * @since 2021/2/8 17:45
 */
public class PageSort {

    private int skip = 0;
    private int limit = 10;

    private List<FieldSort> sort = new ArrayList<>(1);

    public int getSkip() {
        if(skip == 0){
            return skip;
        }
        return skip - 1;
    }

    public int getFrom(){
        return getSkip() * limit;
    }

    public List<Sort.Order> getSorts(){
        sort.removeIf(fieldSort -> ObjectUtils.isEmpty(fieldSort.getField()) || ObjectUtils.isEmpty(fieldSort.getSort()));
        if (ObjectUtils.isEmpty( sort )) {
            sort = new ArrayList<>();
            sort.add( new FieldSort("createAt","desc") );
        }
        List<Sort.Order> sorts = new ArrayList<>();
        for (FieldSort fieldSort : sort) {
            String sort = fieldSort.getSort();
            if (sort.equalsIgnoreCase( "desc" )) {
                sorts.add( Sort.Order.desc( fieldSort.getField() ) );
            } else if (sort.equalsIgnoreCase( "asc" )) {
                sorts.add( Sort.Order.asc( fieldSort.getField() ) );
            }
        }
        return sorts;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<FieldSort> getSort() {
        return sort;
    }

    public void setSort(List<FieldSort> sort) {
        this.sort = sort;
    }
}
