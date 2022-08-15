package cn.hiboot.mcn.cloud.encryptor.sm2;

import cn.hiboot.mcn.autoconfigure.web.swagger.IgnoreApi;
import cn.hiboot.mcn.cloud.encryptor.sm4.EncryptorProperties;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SM2AutoConfiguration
 *
 * @author DingHao
 * @since 2022/8/11 10:16
 */
@AutoConfiguration
@ConditionalOnClass({SymmetricCrypto.class, Hex.class})
@EnableConfigurationProperties(EncryptorProperties.class)
@ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm2",name = "private-key")
@Import(SM2Encryptor.class)
public class SM2AutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm2",name = "public-key")
    @ConditionalOnMissingBean
    TextEncryptor sm2Encryptor(EncryptorProperties encryptorProperties){
        return new SM2Encryptor(encryptorProperties);
    }

    @RestController
    static class EncryptorController {

        private final TextEncryptor textEncryptor;

        public EncryptorController(TextEncryptor textEncryptor) {
            this.textEncryptor = textEncryptor;
        }

        @PostMapping("_publicKey_")
        public String publicKey() {
            return textEncryptor.publicKey();
        }

        @IgnoreApi
        @PostMapping("__privateKey_")
        public String privateKey() {
            return textEncryptor.privateKey();
        }

    }
}
