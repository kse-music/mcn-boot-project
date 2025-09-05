package cn.hiboot.mcn.autoconfigure.jpa.predicate;

import cn.hiboot.mcn.autoconfigure.jpa.AbstractPredicateProvider;
import cn.hiboot.mcn.core.util.McnUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;

/**
 * ValueInPredicate
 *
 * @author DingHao
 * @since 2022/1/22 11:29
 */
public class ValueInPredicate<T, E> extends AbstractPredicateProvider<T> {

    private final List<E> valueList;

    public ValueInPredicate(String fieldName, List<E> valueList) {
        super(fieldName);
        this.valueList = valueList;
    }

    @Override
    public boolean isValid() {
        return McnUtils.isNotNullAndEmpty(valueList);
    }

    @Override
    public Predicate doGetPredicate(Root<T> root, CriteriaBuilder criteriaBuilder) {
        return root.get(getFieldName()).in(valueList);
    }

}