package cn.hiboot.mcn.cloud.encryptor.sm2;

import cn.hiboot.mcn.cloud.encryptor.sm4.EncryptorProperties;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SM2AutoConfiguration
 *
 * @author DingHao
 * @since 2022/8/11 10:16
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SymmetricCrypto.class, Hex.class})
@EnableConfigurationProperties(EncryptorProperties.class)
@ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm2",name = "private-key")
@Import(SM2AutoConfiguration.SM2Encryptor.class)
public class SM2AutoConfiguration {

    @ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm2",name = "public-key")
    protected static class SM2Encryptor implements TextEncryptor {
        private final boolean continueOnError;
        private final SM2 sm2;

        public SM2Encryptor(EncryptorProperties encryptorProperties) {
            this.continueOnError = encryptorProperties.isContinueOnError();
            EncryptorProperties.SM2 config = encryptorProperties.getSm2();
            SM2 sm2 = SmUtil.sm2(config.getPrivateKey(),config.getPublicKey());
            sm2.setMode(SM2Engine.Mode.valueOf(config.getMode().name()));
            this.sm2 = sm2;
        }

        @Override
        public String encrypt(String text) {
            try {
                return sm2.encryptBcd(text, KeyType.PublicKey);
            }catch (Exception e){
                if(!continueOnError){
                    throw e;
                }
            }
            return text;
        }

        @Override
        public String decrypt(String text) {
            try {
                return sm2.decryptStr(text, KeyType.PrivateKey);
            }catch (Exception e){
                if(!continueOnError){
                    throw e;
                }
            }
            return text;
        }

    }

    @RestController
    static class EncryptorController {

        private final EncryptorProperties encryptorProperties;

        public EncryptorController(EncryptorProperties encryptorProperties) {
            this.encryptorProperties = encryptorProperties;
        }

        @PostMapping("_public_key_")
        public String publicKey() {
            return encryptorProperties.getSm2().getPublicKey();
        }

    }
}
