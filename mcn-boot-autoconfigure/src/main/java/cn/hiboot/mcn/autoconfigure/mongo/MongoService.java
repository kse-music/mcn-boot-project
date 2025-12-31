package cn.hiboot.mcn.autoconfigure.mongo;

import cn.hiboot.mcn.autoconfigure.jpa.JpaUtils;
import cn.hiboot.mcn.core.model.base.FieldSort;
import cn.hiboot.mcn.core.model.base.PageSort;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * MongoService
 *
 * @author DingHao
 * @since 2025/8/6 16:31
 */
public interface MongoService<T, ID, R extends MongoRepository<T, ID>> {

    default R getRepository() {
        return McnUtils.getRepository(this.getClass(), MongoService.class::isAssignableFrom);
    }

    default T save(T data) {
        beforeSave(data);
        return getRepository().save(data);
    }

    default void beforeSave(T data) {
    }

    default List<T> saveAll(List<T> data) {
        return getRepository().saveAll(data);
    }

    default void deleteById(ID id) {
        deleteByIds(Collections.singletonList(id));
    }

    default void deleteByIds(Collection<ID> ids) {
        getRepository().deleteAllById(ids);
    }

    default T getById(ID id) {
        return getRepository().findById(id).orElse(null);
    }

    default List<T> getByIds(Collection<ID> ids) {
        Iterable<T> iterable = getRepository().findAllById(ids);
        List<T> list = new ArrayList<>();
        for (T t : iterable) {
            list.add(t);
        }
        return list;
    }

    default List<T> list(T t) {
        return list(t, Collections.emptyList());
    }

    default List<T> list(T t, List<FieldSort> sort) {
        PageSort pageSort = new PageSort(sort);
        return page(t, pageSort).getData();
    }

    default RestResp<List<T>> page(T t, PageSort pageSort) {
        PageRequest pageRequest = PageRequest.of(pageSort.getPageIndex(),pageSort.getPageSize(),JpaUtils.jpaSort(pageSort.getSort()));
        Page<T> page = getRepository().findAll(Example.of(t), pageRequest);
        return RestResp.ok(page.getContent(), page.getTotalElements());
    }

    default void updateById(ID id, T data) {
        getRepository().findById(id).ifPresent(d -> {
            beforeUpdate(d,data);
            JpaUtils.copyTo(data,d);
            getRepository().save(d);
        });
    }

    default void beforeUpdate(T oldData,T newData){

    }

    default long count(T t) {
        return getRepository().count(Example.of(t));
    }


}
