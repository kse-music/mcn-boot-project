package cn.hiboot.mcn.autoconfigure.jpa;

import java.util.Arrays;
import java.util.Collections;

/**
 * Specifications
 *
 * @author DingHao
 * @since 2022/1/24 11:01
 */
public interface Specifications {

    static <S> ExampleSpecification<S> of(PredicateProvider<S> predicateProviders){
        return new ExampleSpecification<>(null, Collections.singletonList(predicateProviders),null);
    }

    static <S> ExampleSpecification<S> withOf(S t, PredicateProvider<S> predicateProviders){
        return new ExampleSpecification<>(t, Collections.singletonList(predicateProviders),null);
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> and(PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(null,Arrays.asList(predicateProviders),null);
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> withAnd(S t, PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(t, Arrays.asList(predicateProviders),null);
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> or(PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(null,null,Arrays.asList(predicateProviders));
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> withOr(S t, PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(t,null, Arrays.asList(predicateProviders));
    }

    static <S> ExampleSpecification.Builder<S> builder(){
        return new ExampleSpecification.Builder<>();
    }

}
