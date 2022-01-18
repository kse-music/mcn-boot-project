package cn.hiboot.mcn.core.service;


import cn.hiboot.mcn.core.mapper.BaseMapper;
import cn.hiboot.mcn.core.model.result.RestResp;

import java.util.List;

public interface BaseService<T,PK,R extends BaseMapper<T,PK>> {

    R getMapper();

    default T save(T pojo){
        getMapper().insert(pojo);
        return pojo;
    }

    default T saveSelective(T pojo){
        getMapper().insertSelective(pojo);
        return pojo;
    }

    default void deleteByPrimaryKey(PK id){
        getMapper().deleteByPrimaryKey(id);
    }

    default T getByPrimaryKey(PK id){
        return getMapper().selectByPrimaryKey(id);
    }

    default void updateByPrimaryKeySelective(T pojo){
        getMapper().updateByPrimaryKeySelective(pojo);
    }

    default RestResp<List<T>> listPage(T pojo){
        return new RestResp<>(pageSelect(pojo),pageCount(pojo));
    }

    default List<T> pageSelect(T pojo){
        return getMapper().pageSelect(pojo);
    }

    default int pageCount(T pojo){
        return getMapper().pageCount(pojo);
    }

    default List<T> selectByCondition(T pojo){
        return getMapper().selectByCondition(pojo);
    }

}
