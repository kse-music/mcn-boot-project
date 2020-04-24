package cn.hiboot.mcn.core.service;

import cn.hiboot.mcn.core.mapper.BaseMapper;
import cn.hiboot.mcn.core.model.result.RestResp;

import java.util.List;

public class BaseServiceImpl<T,PK> implements BaseService<T,PK> {

    @McnAutowired
    private BaseMapper<T,PK> baseMapper;

    @Override
    public void assertSelfData(PK id) {

    }

    @Override
    public T save(T pojo) {
        baseMapper.insert(pojo);
        return pojo;
    }

    @Override
    public T saveSelective(T pojo) {
        baseMapper.insertSelective(pojo);
        return pojo;
    }

    @Override
    public void deleteByPrimaryKey(PK id) {
        baseMapper.deleteByPrimaryKey(id);
    }

    @Override
    public T getByPrimaryKey(PK id) {
        return baseMapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateByPrimaryKeySelective(T pojo) {
        baseMapper.updateByPrimaryKeySelective(pojo);
    }

    @Override
    public RestResp<List<T>> listPage(T pojo) {
        return new RestResp<>(pageSelect(pojo),pageCount(pojo));
    }

    @Override
    public List<T> pageSelect(T pojo) {
        return baseMapper.pageSelect(pojo);
    }

    @Override
    public int pageCount(T pojo) {
        return baseMapper.pageCount(pojo);
    }

    @Override
    public List<T> selectByCondition(T pojo) {
        return baseMapper.selectByCondition(pojo);
    }
}
