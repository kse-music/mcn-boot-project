package cn.hiboot.mcn.autoconfigure.db;

import cn.hiboot.mcn.autoconfigure.context.McnPropertiesPostProcessor;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnSingleDatasourceCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        String[] dbs = environment.getProperty(MultipleMybatisAutoConfiguration.PREFIX+"name", String[].class);
        if(dbs == null || dbs.length == 0) {
            ClassPathMapperScanner scanner = new ClassPathMapperScanner(context.getRegistry());
            ResourceLoader resourceLoader = context.getResourceLoader();
            if (resourceLoader != null) {
                scanner.setResourceLoader(resourceLoader);
            }
            scanner.registerFilters();
            String daoPackage = environment.getProperty((McnPropertiesPostProcessor.APP_BASE_PACKAGE))+".dao";
            scanner.doScan(daoPackage);
        }
        return false;
    }

}
