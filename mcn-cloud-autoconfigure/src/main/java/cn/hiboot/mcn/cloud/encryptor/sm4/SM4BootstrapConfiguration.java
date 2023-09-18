package cn.hiboot.mcn.cloud.encryptor.sm4;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
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
@Import(SM4BootstrapConfiguration.SM4Encryptor.class)
public class SM4BootstrapConfiguration {

    @ConditionalOnClass({SymmetricCrypto.class,Hex.class})
    @ConditionalOnMissingBean(TextEncryptor.class)
    private static class SM4Encryptor implements TextEncryptor {

        private final boolean base64;
        private final SymmetricCrypto sm4;
        private final boolean continueOnError;

        public SM4Encryptor(EncryptorProperties encryptorProperties) {
            this.continueOnError = encryptorProperties.isContinueOnError();
            EncryptorProperties.SM4 sm4 = encryptorProperties.getSm4();
            if(sm4.getMode() != null && sm4.getPadding() != null){
                Mode mode = Mode.valueOf(sm4.getMode().name());
                Padding padding = Padding.valueOf(sm4.getPadding().name());
                if(sm4.getIv() == null){
                    this.sm4 = new SM4(mode,padding,sm4.getKey().getBytes(StandardCharsets.UTF_8));
                }else {
                    this.sm4 = new SM4(mode,padding,sm4.getKey().getBytes(StandardCharsets.UTF_8),sm4.getIv().getBytes(StandardCharsets.UTF_8));
                }
            }else {
                this.sm4 = SmUtil.sm4(sm4.getKey().getBytes(StandardCharsets.UTF_8));
            }
            this.base64 = encryptorProperties.getSm4().isBase64();
        }

        @Override
        public String encrypt(String s) {
            try {
                return base64 ? sm4.encryptBase64(s) : sm4.encryptHex(s);
            }catch (Exception e){
                if(!continueOnError){
                    throw e;
                }
            }
            return s;
        }

        @Override
        public String decrypt(String s) {
            try {
                return sm4.decryptStr(s);
            }catch (Exception e){
                if(!continueOnError){
                    throw e;
                }
            }
            return s;
        }

    }

}
