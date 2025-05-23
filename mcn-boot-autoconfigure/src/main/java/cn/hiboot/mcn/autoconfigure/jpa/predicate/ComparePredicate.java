package cn.hiboot.mcn.autoconfigure.jpa.predicate;

import cn.hiboot.mcn.autoconfigure.jpa.AbstractPredicateProvider;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

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
    protected Predicate doGetPredicate(Root<T> root, CriteriaBuilder criteriaBuilder) {
        Path<E> field = root.get(getFieldName());
        return switch (operator) {
            case lt -> criteriaBuilder.lessThan(field, this.value);
            case lte -> criteriaBuilder.lessThanOrEqualTo(field, this.value);
            case eq -> criteriaBuilder.equal(field, this.value);
            case neq -> criteriaBuilder.notEqual(field, this.value);
            case gt -> criteriaBuilder.greaterThan(field, this.value);
            case gte -> criteriaBuilder.greaterThanOrEqualTo(field, this.value);
        };
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    public enum Operator {
        lt,lte,eq,neq,gt,gte,
    }

}
