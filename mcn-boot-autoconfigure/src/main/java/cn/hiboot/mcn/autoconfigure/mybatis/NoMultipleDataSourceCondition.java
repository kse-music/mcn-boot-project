package cn.hiboot.mcn.autoconfigure.mybatis;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * NoMultipleDataSourceCondition
 *
 * @author DingHao
 * @since 2022/1/2 22:20
 */
class NoMultipleDataSourceCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String[] dbs = context.getEnvironment().getProperty(MybatisQuickAutoConfiguration.MULTIPLY_DATASOURCE_CONFIG_KEY, String[].class);
        return dbs == null || dbs.length == 0;
    }

}
