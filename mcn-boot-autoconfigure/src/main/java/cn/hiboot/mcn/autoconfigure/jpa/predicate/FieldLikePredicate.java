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

    private static final String LIKE = "%";
    private final String value;

    private boolean isNot;
    private boolean prefix;
    private boolean suffix;

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
        if(prefix){
            pattern = LIKE + value;
        }else if(suffix){
            pattern = value + LIKE;
        }else {
            pattern = LIKE + value + LIKE;
        }
        Path<String> path = root.get(getFieldName());
        return isNot ? criteriaBuilder.notLike(path,pattern):criteriaBuilder.like(path,pattern);
    }

    public FieldLikePredicate<T> not(){
        this.isNot = true;
        return this;
    }

    private FieldLikePredicate<T> prefix(){
        this.prefix = true;
        return this;
    }

    private FieldLikePredicate<T> suffix(){
        this.suffix = true;
        return this;
    }

}
