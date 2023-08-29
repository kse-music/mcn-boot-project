package cn.hiboot.mcn.autoconfigure.jpa.predicate;

import cn.hiboot.mcn.autoconfigure.jpa.AbstractPredicateProvider;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Date;
import java.util.Objects;

/**
 * DateBetweenPredicate
 *
 * @author DingHao
 * @since 2022/1/22 11:28
 */
public class DateBetweenPredicate<T> extends AbstractPredicateProvider<T> {

    private final Date startTime;
    private final Date endTime;

    public DateBetweenPredicate(String fieldName, Date startTime, Date endTime) {
        super(fieldName);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public boolean isValid() {
        return Objects.nonNull(startTime) && Objects.nonNull(endTime);
    }

    @Override
    protected Predicate doGetPredicate(Root<T> root, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.between(root.get(getFieldName()),startTime,endTime);
    }

}
