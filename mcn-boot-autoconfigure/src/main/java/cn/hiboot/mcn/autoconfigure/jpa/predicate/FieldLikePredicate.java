package cn.hiboot.mcn.autoconfigure.jpa.predicate;

import cn.hiboot.mcn.autoconfigure.jpa.AbstractPredicateProvider;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Objects;

/**
 * FieldLikePredicate
 *
 * @author DingHao
 * @since 2022/1/22 16:11
 */
public class FieldLikePredicate<T> extends AbstractPredicateProvider<T> {
    private final String value;

    private boolean isNot;
    private boolean onlyAddPrefix;
    private boolean onlyAddSuffix;

    public FieldLikePredicate(String fieldName, String value) {
        super(fieldName);
        this.value = value;
    }

    @Override
    public boolean isValid() {
        return Objects.nonNull(value);
    }

    @Override
    public Predicate doGetPredicate(Root<T> root, CriteriaBuilder criteriaBuilder) {
        String pattern;
        if(onlyAddPrefix){
            pattern = "%" + value;
        }else if(onlyAddSuffix){
            pattern = value + "%";
        }else {
            pattern = "%" + value + "%";
        }
        Path<String> path = root.get(getFieldName());
        return isNot ? criteriaBuilder.notLike(path,pattern):criteriaBuilder.like(path,pattern);
    }

    public FieldLikePredicate<T> not(){
        this.isNot = true;
        return this;
    }

    private FieldLikePredicate<T> addPrefix(){
        this.onlyAddPrefix = true;
        return this;
    }

    private FieldLikePredicate<T> addSuffix(){
        this.onlyAddSuffix = true;
        return this;
    }

}
