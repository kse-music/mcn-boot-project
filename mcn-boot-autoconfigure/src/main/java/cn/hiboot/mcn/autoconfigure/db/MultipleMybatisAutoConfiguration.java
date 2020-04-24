package cn.hiboot.mcn.autoconfigure.db;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SqlSessionFactory.class})
@ConditionOnMultipleDatasource(prefix = "multiply.datasource",name = "name")
public class MultipleMybatisAutoConfiguration {

    public static final String PREFIX = "multiply.datasource.";

    @Import(MapperScannerRegistry.class)
    static class MultiplyMapperScanner{

    }

}
