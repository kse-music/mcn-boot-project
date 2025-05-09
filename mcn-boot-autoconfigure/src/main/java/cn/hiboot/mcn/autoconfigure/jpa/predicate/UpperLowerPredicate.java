package cn.hiboot.mcn.autoconfigure.jpa.predicate;

import cn.hiboot.mcn.autoconfigure.jpa.AbstractPredicateProvider;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Objects;

/**
 * UpperLowerPredicate
 *
 * @author DingHao
 * @since 2025/5/9 11:12
 */
public class UpperLowerPredicate<T, E extends Comparable<? super E>> extends AbstractPredicateProvider<T> {

    private final E lower;
    private final E upper;
    private final boolean withEq;

    public UpperLowerPredicate(String fieldName, E lower, E upper) {
        this(fieldName, lower, upper, false);
    }

    public UpperLowerPredicate(String fieldName, E lower, E upper, boolean withEq) {
        super(fieldName);
        this.lower = lower;
        this.upper = upper;
        this.withEq = withEq;
    }

    @Override
    public boolean isValid() {
        return Objects.nonNull(lower) && Objects.nonNull(upper);
    }

    @Override
    protected Predicate doGetPredicate(Root<T> root, CriteriaBuilder criteriaBuilder) {
        Path<E> path = root.get(getFieldName());
        if (withEq) {
            return criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(path, lower), criteriaBuilder.lessThanOrEqualTo(path, upper));
        } else {
            return criteriaBuilder.and(criteriaBuilder.greaterThan(path, lower), criteriaBuilder.lessThan(path, upper));
        }
    }

}