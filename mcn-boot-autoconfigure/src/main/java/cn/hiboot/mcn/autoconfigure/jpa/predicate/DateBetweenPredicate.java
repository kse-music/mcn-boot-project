package cn.hiboot.mcn.autoconfigure.jpa.predicate;

import cn.hiboot.mcn.autoconfigure.jpa.PredicateProvider;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

/**
 * DateBetweenPredicate
 *
 * @author DingHao
 * @since 2022/1/22 11:28
 */
public class DateBetweenPredicate<T> implements PredicateProvider<T> {

    private final String fieldName;
    private final Date startTime;
    private final Date endTime;

    public DateBetweenPredicate(String fieldName, Date startTime, Date endTime) {
        this.fieldName = fieldName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public Predicate getRestriction(Root<T> root, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.between(root.get(fieldName),startTime,endTime);
    }

}
