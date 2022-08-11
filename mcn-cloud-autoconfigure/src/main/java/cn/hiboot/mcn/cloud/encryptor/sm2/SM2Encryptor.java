package cn.hiboot.mcn.cloud.encryptor.sm2;

import cn.hiboot.mcn.cloud.encryptor.sm4.EncryptorProperties;
import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * SM2Encryptor
 *
 * @author DingHao
 * @since 2022/8/11 14:14
 */
@ConditionalOnProperty(prefix = EncryptorProperties.KEY+".sm2",name = "public-key")
public class SM2Encryptor implements TextEncryptor {
    private final EncryptorProperties.SM2 config;
    private final boolean continueOnError;
    private final SM2 sm2;

    public SM2Encryptor(EncryptorProperties encryptorProperties) {
        this.continueOnError = encryptorProperties.isContinueOnError();
        this.config = encryptorProperties.getSm2();
        SM2 sm2 = SmUtil.sm2(config.getPrivateKey(),config.getPublicKey());
        sm2.setMode(SM2Engine.Mode.valueOf(config.getMode().name()));
        this.sm2 = sm2;
    }

    SM2 getSm2() {
        return sm2;
    }

    @Override
    public String encrypt(String text) {
        try {
            String encryptBcd = sm2.encryptBcd(text, KeyType.PublicKey);
            return config.isLowerCase() ? encryptBcd.toLowerCase() : encryptBcd;
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
            String decryptStr = sm2.decryptStr(text, KeyType.PrivateKey);
            if(config.isBase64()){
                return Base64.decodeStr(decryptStr);
            }
            return decryptStr;
        }catch (Exception e){
            if(!continueOnError){
                throw e;
            }
        }
        return text;
    }

}
