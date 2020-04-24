package cn.hiboot.mcn.core.service;


import cn.hiboot.mcn.core.model.result.RestResp;

import java.util.List;

public interface BaseService<T,PK> {

    void assertSelfData(PK id);
    T save(T pojo);
    T saveSelective(T pojo);
    void deleteByPrimaryKey(PK id);
    T getByPrimaryKey(PK id);
    void updateByPrimaryKeySelective(T pojo);
    RestResp<List<T>> listPage(T pojo);
    List<T> pageSelect(T pojo);
    int pageCount(T pojo);
    List<T> selectByCondition(T pojo);

}
