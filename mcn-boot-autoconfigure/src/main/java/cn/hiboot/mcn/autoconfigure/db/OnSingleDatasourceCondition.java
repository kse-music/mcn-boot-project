package cn.hiboot.mcn.autoconfigure.db;

import cn.hiboot.mcn.autoconfigure.context.McnPropertiesPostProcessor;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 单数据源自动扫描basePkg+dao包下的mapper接口
 *
 * @author DingHao
 * @since 2021/6/30 15:21
 */
public class OnSingleDatasourceCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        String[] dbs = environment.getProperty(MultipleMybatisAutoConfiguration.PREFIX+"name", String[].class);
        if(dbs == null || dbs.length == 0) {//无多数据源配置即认为是单数据源
            ClassPathMapperScanner scanner = new ClassPathMapperScanner(context.getRegistry());
            ResourceLoader resourceLoader = context.getResourceLoader();
            scanner.setResourceLoader(resourceLoader);
            scanner.registerFilters();
            String daoPackage = environment.getProperty((McnPropertiesPostProcessor.APP_BASE_PACKAGE))+".dao";
            scanner.doScan(daoPackage);
        }
        return false;
    }

}
