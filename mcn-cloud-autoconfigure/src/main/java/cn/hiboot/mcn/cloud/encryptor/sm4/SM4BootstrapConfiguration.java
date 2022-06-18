package cn.hiboot.mcn.cloud.encryptor.sm4;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.sgitg.sgcc.sm.SM4Utils;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
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
@AutoConfiguration
@ConditionalOnClass(TextEncryptor.class)
@EnableConfigurationProperties(EncryptorProperties.class)
@Order(0)
@ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm4",name = "key")
public class SM4BootstrapConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(SymmetricCrypto.class)
    @ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm4",name = "use-default",havingValue = "true",matchIfMissing = true)
    private static class SM4Encryptor implements TextEncryptor {

        private final boolean base64;
        private final SymmetricCrypto sm4;

        public SM4Encryptor(EncryptorProperties encryptorProperties) {
            EncryptorProperties.SM4 sm4 = encryptorProperties.getSm4();
            if(sm4.getMode() != null && sm4.getPadding() != null){
                if(sm4.getIv() == null){
                    this.sm4 = new SM4(sm4.getMode(),sm4.getPadding(),sm4.getKey().getBytes(StandardCharsets.UTF_8));
                }else {
                    this.sm4 = new SM4(sm4.getMode(),sm4.getPadding(),sm4.getKey().getBytes(StandardCharsets.UTF_8),sm4.getIv().getBytes(StandardCharsets.UTF_8));
                }
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

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(SM4Utils.class)
    @Conditional(SM4ExtendConfig.SM4ExtendCondition.class)
    private static class SM4ExtendConfig {

        private static class SM4Extend extends SM4Utils implements TextEncryptor {

            private final byte[] key;
            private final byte[] iv;

            public SM4Extend(EncryptorProperties encryptorProperties) {
                this.key = generateKeyOrIV(encryptorProperties.getSm4().getKey());
                this.iv = generateKeyOrIV(encryptorProperties.getSm4().getIv());
            }

            private byte[] generateKeyOrIV(String str) {
                return Hex.encode(str.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public String encrypt(String text) {
                return new String(Hex.encode(encryptData_CBC(Hex.decode(iv), Hex.decode(key), Strings.toUTF8ByteArray(text))));
            }

            @Override
            public String decrypt(String encryptedText) {
                return Strings.fromUTF8ByteArray(decryptData_CBC(Hex.decode(iv), Hex.decode(key), Hex.decode(encryptedText))).trim();
            }
        }

        static class SM4ExtendCondition extends AllNestedConditions {

            SM4ExtendCondition() {
                super(ConfigurationPhase.REGISTER_BEAN);
            }

            @ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm4",name = "mode",havingValue = "cbc")
            static class CBCModeEnabled {

            }

            @ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm4",name = "use-default",havingValue = "false")
            static class NotUseDefaultEnabled {

            }

        }

    }

}
