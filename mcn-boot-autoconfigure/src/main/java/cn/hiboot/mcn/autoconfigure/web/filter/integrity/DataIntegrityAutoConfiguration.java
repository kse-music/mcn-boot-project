package cn.hiboot.mcn.autoconfigure.web.filter.integrity;

import cn.hiboot.mcn.autoconfigure.web.security.WebSecurityProperties;
import cn.hutool.crypto.SmUtil;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DataIntegrity
 *
 * @author DingHao
 * @since 2022/6/4 23:39
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SmUtil.class, SM3Digest.class})
@EnableConfigurationProperties({DataIntegrityProperties.class, WebSecurityProperties.class})
@ConditionalOnProperty(prefix = "data.integrity",name = "enable",havingValue = "true")
public class DataIntegrityAutoConfiguration {

    private final DataIntegrityProperties dataIntegrityProperties;

    public DataIntegrityAutoConfiguration(DataIntegrityProperties dataIntegrityProperties) {
        this.dataIntegrityProperties = dataIntegrityProperties;
    }

    @Bean
    public DataIntegrityFilter dataIntegrityInterceptor(WebSecurityProperties webSecurityProperties){
        return new DataIntegrityFilter(dataIntegrityProperties,webSecurityProperties);
    }

}
