package cn.hiboot.mcn.cloud.encryptor.sm4;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.nio.charset.StandardCharsets;

/**
 * SM4加密配置
 *
 * @author DingHao
 * @since 2022/2/15 14:01
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({TextEncryptor.class,SymmetricCrypto.class})
@EnableConfigurationProperties(EncryptorProperties.class)
@Order(0)
@ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm4",name = "key")
public class SM4BootstrapConfiguration {

    @Configuration(proxyBeanMethods = false)
    private static class SM4Encryptor implements TextEncryptor {

        private final boolean base64;
        private final SymmetricCrypto sm4;

        public SM4Encryptor(EncryptorProperties encryptorProperties) {
            EncryptorProperties.SM4 sm4 = encryptorProperties.getSm4();
            if(sm4.getMode() != null && sm4.getPadding() != null){
                this.sm4 = new SM4(sm4.getMode(),sm4.getPadding(),sm4.getKey().getBytes(StandardCharsets.UTF_8));
            }else {
                this.sm4 = SmUtil.sm4(sm4.getKey().getBytes(StandardCharsets.UTF_8));
            }
            this.base64 = encryptorProperties.getSm4().isBase64();
        }

        @Override
        public String encrypt(String s) {
            return base64 ? sm4.encryptBase64(s) : sm4.encryptHex(s);
        }

        @Override
        public String decrypt(String s) {
            return sm4.decryptStr(s);
        }

    }

}
