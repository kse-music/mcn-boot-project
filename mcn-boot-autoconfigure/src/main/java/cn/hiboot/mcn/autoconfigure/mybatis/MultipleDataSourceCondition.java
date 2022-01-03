package cn.hiboot.mcn.autoconfigure.mybatis;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * MultipleDataSourceCondition
 *
 * @author DingHao
 * @since 2022/1/3 18:12
 */
public class MultipleDataSourceCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String[] dbs = context.getEnvironment().getProperty(MybatisQuickAutoConfiguration.MULTIPLY_DATASOURCE_CONFIG_KEY, String[].class);
        if(dbs != null && dbs.length > 1){
            return ConditionOutcome.match("match multiple datasource");
        }
        return ConditionOutcome.noMatch("no multiple datasource");
    }
}
