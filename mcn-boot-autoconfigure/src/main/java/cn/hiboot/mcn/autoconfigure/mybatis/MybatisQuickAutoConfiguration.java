package cn.hiboot.mcn.autoconfigure.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * MybatisQuickAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/2 0:25
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SqlSessionFactory.class, MapperScan.class})
@ConditionalOnMissingBean(MapperFactoryBean.class)
@Import({SingleDataSourceBeanFactoryPostProcessor.class,MultipleDataSourceBeanFactoryPostProcessor.class})
public class MybatisQuickAutoConfiguration {

    static final String MULTIPLY_DATASOURCE_CONFIG_KEY = "multiply.datasource.name";

}
