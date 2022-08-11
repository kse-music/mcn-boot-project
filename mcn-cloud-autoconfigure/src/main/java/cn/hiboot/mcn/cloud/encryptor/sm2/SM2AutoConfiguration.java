package cn.hiboot.mcn.cloud.encryptor.sm2;

import cn.hiboot.mcn.autoconfigure.web.swagger.IgnoreApi;
import cn.hiboot.mcn.cloud.encryptor.sm4.EncryptorProperties;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
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
@Import(SM2Encryptor.class)
public class SM2AutoConfiguration {

    @RestController
    static class EncryptorController {

        private final SM2Encryptor sm2Encryptor;

        public EncryptorController(SM2Encryptor sm2Encryptor) {
            this.sm2Encryptor = sm2Encryptor;
        }

        @PostMapping("_publicKey_")
        public String publicKey() {
            return HexUtil.encodeHexStr(sm2Encryptor.getSm2().getQ(false));
        }

        @IgnoreApi
        @PostMapping("__privateKey_")
        public String privateKey() {
            return sm2Encryptor.getSm2().getDHex();
        }

    }
}
