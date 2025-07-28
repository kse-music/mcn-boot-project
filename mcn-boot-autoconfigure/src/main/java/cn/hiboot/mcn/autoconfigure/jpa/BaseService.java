package cn.hiboot.mcn.autoconfigure.jpa;

import cn.hiboot.mcn.core.model.CrudService;
import cn.hiboot.mcn.core.model.base.FieldSort;
import cn.hiboot.mcn.core.model.base.PageSort;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;

/**
 * BaseService
 *
 * @author DingHao
 * @since 2021/10/11 13:39
 */
public interface BaseService<T, PK, R extends BaseRepository<T, PK>> extends CrudService<T, PK> {

    default R getRepository() {
        return JpaUtils.getRepository(this.getClass());
    }

    @Override
    default T save(T data){
        beforeSave(data);
        return getRepository().save( data );
    }

    @Override
    default List<T> saveAll(List<T> data){
        return getRepository().saveAll( data );
    }

    default void beforeSave(T data){

    }

    @Override
    default void deleteById(PK id){
        try {
            getRepository().deleteById(id);
        } catch (Exception ignored) {

        }
    }

    @Override
    default void deleteByIds(Collection<PK> ids){
        try {
            getRepository().deleteAllById(ids);
        } catch (Exception ignored) {

        }
    }

    @Override
    default T getById(PK id){
        return getRepository().findById(id).orElse(null);
    }

    @Override
    default List<T> getByIds(Collection<PK> ids){
        return getRepository().findAllById(ids);
    }

    default T getOne(T t){
        return getRepository().findOne(Example.of(t)).orElse(null);
    }

    @Override
    default List<T> list(T t){
        return getRepository().findAll(Example.of(t));
    }

    @Override
    default List<T> list(T t, List<FieldSort> sort){
        return getRepository().findAll(Example.of(t),JpaUtils.jpaSort(sort));
    }

    default RestResp<List<T>> page(T t, PageSort pageSort){
        PageRequest pageRequest = PageRequest.of(pageSort.getPageIndex(),pageSort.getPageSize(),JpaUtils.jpaSort(pageSort.getSort()));
        Page<T> page;
        if(t == null){
            page = getRepository().findAll(pageRequest);
        }else {
            page = getRepository().findAll(Example.of(t),pageRequest);
        }
        return RestResp.ok(page.getContent(),page.getTotalElements());
    }

    default T getOne(Specification<T> s){
        return getRepository().findOne(s).orElse(null);
    }

    default List<T> list(Specification<T> s){
        return getRepository().findAll(s);
    }

    default List<T> list(Specification<T> s, List<FieldSort> sort){
        return getRepository().findAll(s,JpaUtils.jpaSort(sort));
    }

    default RestResp<List<T>> page(Specification<T> s, PageSort pageSort){
        PageRequest pageRequest = PageRequest.of(pageSort.getPageIndex(),pageSort.getPageSize(),JpaUtils.jpaSort(pageSort.getSort()));
        Page<T> page = getRepository().findAll(s,pageRequest);
        return RestResp.ok(page.getContent(),page.getTotalElements());
    }

    @Override
    default void updateById(PK id, T data){
        getRepository().findById(id).ifPresent(d -> {
            beforeUpdate(d,data);
            JpaUtils.copyTo(data,d);
            getRepository().save(d);
        });
    }

    default void beforeUpdate(T oldData, T newData){

    }

    @Override
    default long count(T t) {
        return getRepository().count(Example.of(t));
    }

    default long count(Specification<T> s) {
        return getRepository().count(s);
    }

}
