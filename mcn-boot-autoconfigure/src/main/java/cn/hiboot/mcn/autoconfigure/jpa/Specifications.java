package cn.hiboot.mcn.autoconfigure.jpa;

/**
 * Specifications
 *
 * @author DingHao
 * @since 2022/1/24 11:01
 */
public interface Specifications {

    static <S> ExampleSpecification<S> of(PredicateProvider<S> predicateProviders){
        return and(predicateProviders);
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> and(PredicateProvider<S>... predicateProviders){
        return withAnd(null, predicateProviders);
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> withAnd(S t, PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(t,predicateProviders);
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> or(PredicateProvider<S>... predicateProviders){
        return withOr(null, predicateProviders);
    }

    @SafeVarargs
    static <S> ExampleSpecification<S> withOr(S t, PredicateProvider<S>... predicateProviders){
        return new ExampleSpecification<>(t, predicateProviders).or();
    }

}
