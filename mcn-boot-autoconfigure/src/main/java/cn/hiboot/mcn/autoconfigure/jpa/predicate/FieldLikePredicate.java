package cn.hiboot.mcn.autoconfigure.jpa.predicate;

import cn.hiboot.mcn.autoconfigure.jpa.AbstractPredicateProvider;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
        return StringUtils.hasText(value);
    }

    @Override
    public Predicate doGetPredicate(Root<T> root, CriteriaBuilder criteriaBuilder) {
        String pattern;
        if (prefix) {
            pattern = LIKE + value;
        } else if (suffix) {
            pattern = value + LIKE;
        } else {
            pattern = LIKE + value + LIKE;
        }
        Expression<String> as = root.get(getFieldName()).as(String.class);
        return isNot ? criteriaBuilder.notLike(as, pattern) : criteriaBuilder.like(as, pattern);
    }

    public FieldLikePredicate<T> not() {
        this.isNot = true;
        return this;
    }

    private FieldLikePredicate<T> prefix() {
        this.prefix = true;
        return this;
    }

    private FieldLikePredicate<T> suffix() {
        this.suffix = true;
        return this;
    }

}
