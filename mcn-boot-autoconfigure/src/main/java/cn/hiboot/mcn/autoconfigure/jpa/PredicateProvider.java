package cn.hiboot.mcn.autoconfigure.jpa;

import org.springframework.lang.Nullable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * PredicateProvider
 *
 * @author DingHao
 * @since 2022/1/22 11:25
 */
public interface PredicateProvider<T> {

    boolean isValid();

    /**
     * JPA 条件扩展查询
     * @param root 查询根
     * @param criteriaBuilder  标准
     * @return 扩展后的查询条件
     */
    @Nullable
    Predicate getPredicate(Root<T> root, CriteriaBuilder criteriaBuilder);

}
