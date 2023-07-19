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
import java.util.List;

/**
 * ExampleSpecification
 *
 * @author DingHao
 * @since 2022/1/22 11:25
 */
public class ExampleSpecification<T> implements Specification<T> {

    private List<PredicateProvider<T>> andPredicateProviders;
    private List<PredicateProvider<T>> orPredicateProviders;
    private Example<T> example;
    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
    private boolean isOr;

    private ExampleSpecification(T t) {
        if(!McnUtils.isFieldAllNull(t)){
            this.example = Example.of(t);
        }
    }

    ExampleSpecification(T t, List<PredicateProvider<T>> andPredicateProviders, List<PredicateProvider<T>> orPredicateProviders) {
        this(t);
        this.andPredicateProviders = andPredicateProviders;
        this.orPredicateProviders = orPredicateProviders;
    }

    public void setAndPredicateProviders(List<PredicateProvider<T>> andPredicateProviders) {
        this.andPredicateProviders = andPredicateProviders;
    }

    public void setOrPredicateProviders(List<PredicateProvider<T>> orPredicateProviders) {
        this.orPredicateProviders = orPredicateProviders;
    }

    public void setOr(boolean or) {
        isOr = or;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        if(example == null && McnUtils.isNullOrEmpty(andPredicateProviders) && McnUtils.isNullOrEmpty(orPredicateProviders)){
            return null;
        }
        Predicate beanPredicate = null;
        if(example != null){
            beanPredicate = criteriaBuilder.and(QueryByExamplePredicateBuilder.getPredicate(root, criteriaBuilder, example, escapeCharacter));
        }
        Predicate[] andPredicates =  predicates(andPredicateProviders,root,criteriaBuilder);
        Predicate[] orPredicates =  predicates(orPredicateProviders,root,criteriaBuilder);
        Predicate logicPredicate = null;
        if(McnUtils.isNotNullAndEmpty(andPredicates)){
            logicPredicate = criteriaBuilder.and(andPredicates);
        }
        if(McnUtils.isNotNullAndEmpty(orPredicates)){
            Predicate orPredicate = criteriaBuilder.or(orPredicates);
            if(logicPredicate == null){
                logicPredicate = orPredicate;
            }else {
                logicPredicate = isOr ? criteriaBuilder.or(logicPredicate,orPredicate) : criteriaBuilder.and(logicPredicate,orPredicate);
            }
        }
        if(beanPredicate == null){
            return logicPredicate;
        }
        return isOr ? criteriaBuilder.or(beanPredicate,logicPredicate) : criteriaBuilder.and(beanPredicate,logicPredicate);
    }

    private Predicate[] predicates(List<PredicateProvider<T>> predicateProviders,Root<T> root, CriteriaBuilder criteriaBuilder){
        if(McnUtils.isNullOrEmpty(predicateProviders)){
            return null;
        }
        List<Predicate> predicates =  new ArrayList<>(predicateProviders.size());
        for (PredicateProvider<T> predicateProvider : predicateProviders) {
            Predicate predicate = predicateProvider.getPredicate(root, criteriaBuilder);
            if(predicate == null){
                continue;
            }
            predicates.add(predicate);
        }
        return predicates.toArray(new Predicate[0]);
    }

    public ExampleSpecification<T> escapeCharacter(EscapeCharacter escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
        return this;
    }

    public static class Builder<B> {
        private PredicateProvider<B>[] andPredicateProviders;
        private PredicateProvider<B>[] orPredicateProviders;
        private B bean;
        private boolean isOr;

        @SafeVarargs
        public final Builder<B> and(PredicateProvider<B>... andPredicateProviders) {
            this.andPredicateProviders = andPredicateProviders;
            return this;
        }

        @SafeVarargs
        public final Builder<B> or(PredicateProvider<B>... orPredicateProviders) {
            this.orPredicateProviders = orPredicateProviders;
            return this;
        }

        public Builder<B> bean(B bean) {
            this.bean = bean;
            return this;
        }

        public Builder<B> globalOr() {
            this.isOr = true;
            return this;
        }

        public ExampleSpecification<B> build(){
            ExampleSpecification<B> exampleSpecification = new ExampleSpecification<>(bean);
            if(andPredicateProviders != null){
                exampleSpecification.setAndPredicateProviders(Arrays.asList(andPredicateProviders));
            }
            if(orPredicateProviders != null){
                exampleSpecification.setOrPredicateProviders(Arrays.asList(orPredicateProviders));
            }
            exampleSpecification.setOr(isOr);
            return exampleSpecification;
        }
    }

}
