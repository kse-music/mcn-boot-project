package cn.hiboot.mcn.autoconfigure.mybatis;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * NoMultipleDataSourceCondition
 *
 * @author DingHao
 * @since 2022/1/3 18:16
 */
public class NoMultipleDataSourceCondition extends MultipleDataSourceCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return ConditionOutcome.inverse(super.getMatchOutcome(context, metadata));
    }

}

