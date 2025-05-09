package cn.hiboot.mcn.autoconfigure.jpa.predicate;

import cn.hiboot.mcn.autoconfigure.jpa.AbstractPredicateProvider;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * ComparePredicate
 *
 * @author DingHao
 * @since 2024/12/31 11:14
 */
public class ComparePredicate<T, E extends Comparable<? super E>> extends AbstractPredicateProvider<T> {

    private final Operator operator;
    private final E value;

    public ComparePredicate(String fieldName, Operator operator, E value) {
        super(fieldName);
        this.operator = operator;
        this.value = value;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    protected Predicate doGetPredicate(Root<T> root, CriteriaBuilder criteriaBuilder) {
        Path<E> field = root.get(getFieldName());
        switch (operator) {
            case lt: return criteriaBuilder.lessThan(field, this.value);
            case lte: return criteriaBuilder.lessThanOrEqualTo(field, this.value);
            case eq: return criteriaBuilder.equal(field, this.value);
            case neq: return criteriaBuilder.notEqual(field, this.value);
            case gt: return criteriaBuilder.greaterThan(field, this.value);
            case gte: return criteriaBuilder.greaterThanOrEqualTo(field, this.value);
            default:
                return null;
        }
    }

    public enum Operator {
        lt,lte,eq,neq,gt,gte,
    }

}
