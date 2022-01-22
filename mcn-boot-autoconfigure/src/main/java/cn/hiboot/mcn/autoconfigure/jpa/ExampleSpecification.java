package cn.hiboot.mcn.autoconfigure.jpa;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * ExampleSpecification
 *
 * @author DingHao
 * @since 2022/1/22 11:25
 */
public class ExampleSpecification<T> implements Specification<T> {

    private final List<PredicateProvider<T>> fieldRestrictionList;
    private final Example<T> example;
    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
    private boolean isOr;

    public ExampleSpecification(List<PredicateProvider<T>> fieldRestrictionList) {
        this(null,fieldRestrictionList);
    }

    public ExampleSpecification(Example<T> example, List<PredicateProvider<T>> fieldRestrictionList) {
        Assert.notEmpty(fieldRestrictionList,"fieldRestrictionList must not empty");
        this.fieldRestrictionList = fieldRestrictionList;
        this.example = example;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        List<Predicate> extPredicates =  new ArrayList<>();
        if(example != null){
            extPredicates.add(QueryByExamplePredicateBuilder.getPredicate(root, criteriaBuilder,example,escapeCharacter));
        }
        for (PredicateProvider<T> fieldRestriction : fieldRestrictionList) {
            extPredicates.add(fieldRestriction.getRestriction(root, criteriaBuilder));
        }
        if(isOr){
            return criteriaBuilder.or(extPredicates.toArray(new Predicate[0]));
        }
        return criteriaBuilder.and(extPredicates.toArray(new Predicate[0]));
    }

    public ExampleSpecification<T> escapeCharacter(EscapeCharacter escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
        return this;
    }

    public ExampleSpecification<T> or() {
        this.isOr = true;
        return this;
    }

}
