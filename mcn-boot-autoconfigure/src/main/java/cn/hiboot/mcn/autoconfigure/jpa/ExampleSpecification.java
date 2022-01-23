package cn.hiboot.mcn.autoconfigure.jpa;

import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.lang.NonNull;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ExampleSpecification
 *
 * @author DingHao
 * @since 2022/1/22 11:25
 */
public class ExampleSpecification<T> implements Specification<T> {

    private final List<PredicateProvider<T>> predicateProviders;
    private Example<T> example;
    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
    private boolean isOr;

    private ExampleSpecification(PredicateProvider<T> predicateProvider) {
        this(Collections.singletonList(predicateProvider));
    }

    private ExampleSpecification(List<PredicateProvider<T>> predicateProviders) {
        this(null,predicateProviders);
    }

    private ExampleSpecification(T t, PredicateProvider<T> predicateProvider) {
        this(t, Collections.singletonList(predicateProvider));
    }

    private ExampleSpecification(T t, List<PredicateProvider<T>> predicateProviders) {
        this.predicateProviders = predicateProviders;
        if(t != null){
            this.example = Example.of(t);
        }
    }

    @SafeVarargs
    public static <S> ExampleSpecification<S> of(PredicateProvider<S>... predicateProviders){
        return and(null,predicateProviders);
    }

    @SafeVarargs
    public static <S> ExampleSpecification<S> and(S t, PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(t, Arrays.asList(predicateProviders));
    }

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        if(example == null && McnUtils.isNullOrEmpty(predicateProviders)){
            return null;
        }
        List<Predicate> predicates =  new ArrayList<>();
        if(example != null){
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, criteriaBuilder,example,escapeCharacter));
        }
        if(predicateProviders != null){
            for (PredicateProvider<T> predicateProvider : predicateProviders) {
                Predicate predicate = predicateProvider.getPredicate(root, criteriaBuilder);
                if(predicate == null){
                    continue;
                }
                predicates.add(predicate);
            }
        }
        if(predicates.isEmpty()){
            return null;
        }
        if(isOr){
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
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
