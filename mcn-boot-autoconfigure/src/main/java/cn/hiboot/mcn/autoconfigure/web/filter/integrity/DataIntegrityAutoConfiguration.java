package cn.hiboot.mcn.autoconfigure.web.filter.integrity;

import cn.hiboot.mcn.autoconfigure.web.filter.integrity.reactive.ReactiveDataIntegrityFilter;
import cn.hiboot.mcn.autoconfigure.web.security.WebSecurityProperties;
import cn.hutool.crypto.SmUtil;
import jakarta.servlet.Filter;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

/**
 * DataIntegrity
 *
 * @author DingHao
 * @since 2022/6/4 23:39
 */
@AutoConfiguration
@ConditionalOnClass({SmUtil.class, SM3Digest.class})
@EnableConfigurationProperties({DataIntegrityProperties.class, WebSecurityProperties.class})
@ConditionalOnProperty(prefix = "data.integrity",name = "enabled",havingValue = "true")
public class DataIntegrityAutoConfiguration {


    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(Filter.class)
    static class ServletDataIntegrityFilterConfig {

        @Bean
        DataIntegrityFilter dataIntegrityFilter(DataIntegrityProperties dataIntegrityProperties){
            return new DataIntegrityFilter(dataIntegrityProperties);
        }

    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnClass(WebFilter.class)
    static class ReactiveDataIntegrityFilterConfig {

        @Bean
        ReactiveDataIntegrityFilter dataIntegrityFilter(DataIntegrityProperties dataIntegrityProperties){
            return new ReactiveDataIntegrityFilter(dataIntegrityProperties);
        }

    }


}
