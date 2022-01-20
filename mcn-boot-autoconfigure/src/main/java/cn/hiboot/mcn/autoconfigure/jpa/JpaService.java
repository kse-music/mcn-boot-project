package cn.hiboot.mcn.autoconfigure.jpa;

import cn.hiboot.mcn.core.model.base.PageSort;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JpaService
 *
 * @author DingHao
 * @since 2021/10/11 13:39
 */
public interface JpaService<T,PK,R extends JpaRepository<T,PK>> {

    R getRepository();

    default T save(T data){
        beforeSave(data);
        return getRepository().save( data );
    }

    default void beforeSave(T data){

    }

    default void deleteById(PK id){
        getRepository().findById(id).ifPresent(u -> getRepository().delete(u));
    }

    default T getById(PK id){
        return getRepository().findById(id).orElse(null);
    }

    default T getOne(T t){
        List<T> list = list(t);
        return list.isEmpty() ? null : list.get(0);
    }

    default List<T> list(T t){
        return getRepository().findAll(Example.of(t));
    }

    default List<T> list(T t,PageSort pageSort){
        return page(t,pageSort).getData();
    }

    default RestResp<List<T>> page(PageSort pageSort){
        return page(null,pageSort);
    }

    default RestResp<List<T>> page(T t, PageSort pageSort){
        PageRequest pageRequest = PageRequest.of(pageSort.getPageNo(),pageSort.getPageSize(),JpaUtils.jpaSort(pageSort.getSort()));
        Page<T> page;
        if(t == null){
            page = getRepository().findAll(pageRequest);
        }else {
            page = getRepository().findAll(Example.of(t),pageRequest);
        }
        return new RestResp<>(page.getContent(),page.getTotalElements());
    }

    default void updateById(PK id,T data){
        getRepository().findById(id).ifPresent(d -> {
            JpaUtils.copyTo(data,d);
            getRepository().save(d);
        });
    }

}
