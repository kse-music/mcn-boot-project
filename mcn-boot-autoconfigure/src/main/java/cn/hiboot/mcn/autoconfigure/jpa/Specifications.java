package cn.hiboot.mcn.autoconfigure.jpa;

import java.util.Arrays;

/**
 * Specifications
 *
 * @author DingHao
 * @since 2022/1/24 11:01
 */
public interface Specifications {

    static <S> ExampleSpecification<S> of(PredicateProvider<S> predicateProviders){
        return new ExampleSpecification<>(predicateProviders);
    }

    static <S> ExampleSpecification<S> of(S t, PredicateProvider<S> predicateProviders){
        return new ExampleSpecification<>(t, predicateProviders);
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> and(PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(Arrays.asList(predicateProviders));
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> and(S t, PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(t, Arrays.asList(predicateProviders));
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> or(PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(Arrays.asList(predicateProviders)).or();
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> or(S t, PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(t, Arrays.asList(predicateProviders)).or();
    }

}
