package cn.hiboot.mcn.core.model;

import cn.hiboot.mcn.core.model.base.FieldSort;
import cn.hiboot.mcn.core.model.base.PageSort;
import cn.hiboot.mcn.core.model.result.RestResp;

import java.util.Collection;
import java.util.List;

/**
 * CrudService
 *
 * @author DingHao
 * @since 2025/7/28 10:48
 */
public interface CrudService<T, PK> {

    default T save(T data) {
        return saveAll(List.of(data)).get(0);
    }

    List<T> saveAll(List<T> data);

    default void deleteById(PK id) {
        deleteByIds(List.of(id));
    }

    void deleteByIds(Collection<PK> ids);

    default T getById(PK id) {
        List<T> list = getByIds(List.of(id));
        return list.isEmpty() ? null : list.get(0);
    }

    List<T> getByIds(Collection<PK> ids);

    List<T> list(T t);

    List<T> list(T t, List<FieldSort> sort);

    RestResp<List<T>> page(T t, PageSort pageSort);

    void updateById(PK id, T data);

    long count(T t);

}
