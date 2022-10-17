package cn.hiboot.mcn.cloud.encryptor.sm2;

import cn.hiboot.mcn.cloud.encryptor.sm4.EncryptorProperties;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * SM2AutoConfiguration
 *
 * @author DingHao
 * @since 2022/8/11 10:16
 */
@AutoConfiguration
@ConditionalOnClass({SymmetricCrypto.class, Hex.class})
@EnableConfigurationProperties(EncryptorProperties.class)
@ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm2",name = {"private-key","public-key"})
public class SM2AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    TextEncryptor sm2Encryptor(EncryptorProperties encryptorProperties){
        return new SM2Encryptor(encryptorProperties);
    }

}
