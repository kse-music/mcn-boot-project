package cn.hiboot.mcn.core.service;


import cn.hiboot.mcn.core.mapper.BaseMapper;
import cn.hiboot.mcn.core.model.result.RestResp;

import java.util.List;

public interface BaseService<T,PK,R extends BaseMapper<T,PK> > {

    R getBaseMapper();

    default void assertSelfData(PK id){

    }

    default T save(T pojo){
        getBaseMapper().insert(pojo);
        return pojo;
    }

    default T saveSelective(T pojo){
        getBaseMapper().insertSelective(pojo);
        return pojo;
    }

    default void deleteByPrimaryKey(PK id){
        getBaseMapper().deleteByPrimaryKey(id);
    }

    default T getByPrimaryKey(PK id){
        return getBaseMapper().selectByPrimaryKey(id);
    }

    default void updateByPrimaryKeySelective(T pojo){
        getBaseMapper().updateByPrimaryKeySelective(pojo);
    }

    default RestResp<List<T>> listPage(T pojo){
        return new RestResp<>(pageSelect(pojo),pageCount(pojo));
    }

    default List<T> pageSelect(T pojo){
        return getBaseMapper().pageSelect(pojo);
    }

    default int pageCount(T pojo){
        return getBaseMapper().pageCount(pojo);
    }

    default List<T> selectByCondition(T pojo){
        return getBaseMapper().selectByCondition(pojo);
    }

}
