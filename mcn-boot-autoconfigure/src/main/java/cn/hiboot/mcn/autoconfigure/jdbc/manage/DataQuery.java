package cn.hiboot.mcn.autoconfigure.jdbc.manage;

import cn.hiboot.mcn.core.model.base.FieldSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DataQuery
 *
 * @author DingHao
 * @since 2025/5/15 17:41
 */
public class DataQuery extends DbQuery {

    private Integer skip;

    private Integer limit;

    private List<FieldQuery> query = new ArrayList<>(1);

    private List<FieldSort> sort = new ArrayList<>(1);

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public List<FieldQuery> getQuery() {
        return query;
    }

    public void setQuery(List<FieldQuery> query) {
        this.query = query;
    }

    public List<FieldSort> getSort() {
        return sort;
    }

    public void setSort(List<FieldSort> sort) {
        this.sort = sort;
    }

    public void addQuery(FieldQuery... query) {
        this.query.addAll(Arrays.asList(query));
    }

    public void addSort(FieldSort... sorts) {
        this.sort.addAll(Arrays.asList(sorts));
    }

}
