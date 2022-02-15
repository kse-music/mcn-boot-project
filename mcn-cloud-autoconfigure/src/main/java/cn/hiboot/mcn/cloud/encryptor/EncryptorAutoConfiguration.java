package cn.hiboot.mcn.cloud.encryptor;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.nio.charset.StandardCharsets;

/**
 * EncryptorAutoConfiguration
 *
 * @author DingHao
 * @since 2022/2/15 14:01
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({TextEncryptor.class})
@EnableConfigurationProperties(EncryptorProperties.class)
public class EncryptorAutoConfiguration implements Ordered {

    private final EncryptorProperties encryptorProperties;

    public EncryptorAutoConfiguration(EncryptorProperties encryptorProperties) {
        this.encryptorProperties = encryptorProperties;
    }

    @Override
    public int getOrder() {
        return encryptorProperties.getOrder();
    }

    @ConditionalOnClass(SymmetricCrypto.class)
    @Configuration(proxyBeanMethods = false)
    private static class SM4Config implements TextEncryptor{

        private final SymmetricCrypto sm4;

        public SM4Config(EncryptorProperties encryptorProperties) {
            this.sm4 = SmUtil.sm4(encryptorProperties.getSm4().getKey().getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String encrypt(String s) {
            return sm4.encryptHex(s);
        }

        @Override
        public String decrypt(String s) {
            return sm4.decryptStr(s);
        }

    }

}
