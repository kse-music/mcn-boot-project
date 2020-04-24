package cn.hiboot.mcn.core.mapper;

import java.util.List;

public interface BaseMapper<T,PK> {

    int insert(T pojo);
    int insertSelective(T record);
    int deleteByPrimaryKey(PK id);
    T selectByPrimaryKey(PK id);
    int updateByPrimaryKeySelective(T pojo);
    List<T> pageSelect(T pojo);
    int pageCount(T pojo);
    List<T> selectByCondition(T pojo);

}
