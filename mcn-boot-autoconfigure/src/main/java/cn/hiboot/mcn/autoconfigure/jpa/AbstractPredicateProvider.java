package cn.hiboot.mcn.autoconfigure.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.util.Assert;

/**
 * AbstractPredicateProvider
 *
 * @author DingHao
 * @since 2022/1/22 16:42
 */
public abstract class AbstractPredicateProvider<T> implements PredicateProvider<T>{

    private final String fieldName;

    public AbstractPredicateProvider(String fieldName) {
        Assert.hasText(fieldName,"fieldName must not empty");
        this.fieldName = fieldName;
    }

    @Override
    public Predicate getPredicate(Root<T> root, CriteriaBuilder criteriaBuilder) {
        if(isValid()){
            return doGetPredicate(root, criteriaBuilder);
        }
        return null;
    }

    protected abstract Predicate doGetPredicate(Root<T> root, CriteriaBuilder criteriaBuilder);

    public String getFieldName() {
        return fieldName;
    }
}
