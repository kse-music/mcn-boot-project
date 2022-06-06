package cn.hiboot.mcn.autoconfigure.web.filter.integrity;

import cn.hutool.crypto.SmUtil;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * DataIntegrity
 *
 * @author DingHao
 * @since 2022/6/4 23:39
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SmUtil.class, SM3Digest.class})
@EnableConfigurationProperties(DataIntegrityProperties.class)
@ConditionalOnProperty(prefix = "data.integrity",name = "check",havingValue = "true")
public class DataIntegrityAutoConfiguration {

    private final DataIntegrityProperties dataIntegrityProperties;

    public DataIntegrityAutoConfiguration(DataIntegrityProperties dataIntegrityProperties) {
        this.dataIntegrityProperties = dataIntegrityProperties;
    }

    @Bean
    public DataIntegrityFilter dataIntegrityInterceptor(){
        return new DataIntegrityFilter(dataIntegrityProperties);
    }

}
