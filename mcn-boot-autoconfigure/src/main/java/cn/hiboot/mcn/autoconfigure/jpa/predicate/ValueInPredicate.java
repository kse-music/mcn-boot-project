package cn.hiboot.mcn.autoconfigure.jpa.predicate;

import cn.hiboot.mcn.autoconfigure.jpa.PredicateProvider;
import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * ValueInPredicate
 *
 * @author DingHao
 * @since 2022/1/22 11:29
 */
public class ValueInPredicate<T, E> implements PredicateProvider<T> {
    private final List<E> valueList;
    private final String fieldName;

    public ValueInPredicate(String fieldName, List<E> valueList) {
        Assert.notEmpty(valueList,"valueList must not empty");
        this.valueList = valueList;
        this.fieldName = fieldName;
    }

    @Override
    public Predicate getRestriction(Root<T> root, CriteriaBuilder criteriaBuilder) {
        return root.get(fieldName).in(valueList);
    }

}