package cn.hiboot.mcn.cloud.encryptor.sm2;

import cn.hiboot.mcn.cloud.encryptor.sm4.EncryptorProperties;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import org.bouncycastle.crypto.engines.SM2Engine;

/**
 * SM2Encryptor
 *
 * @author DingHao
 * @since 2022/8/11 14:14
 */
public class SM2Encryptor implements TextEncryptor {
    private final EncryptorProperties.SM2 config;
    private final boolean continueOnError;
    private final SM2 sm2;

    public SM2Encryptor(EncryptorProperties encryptorProperties) {
        this.continueOnError = encryptorProperties.isContinueOnError();
        this.config = encryptorProperties.getSm2();
        this.sm2 = SmUtil.sm2(config.getPrivateKey(), config.getPublicKey());
        sm2.setMode(SM2Engine.Mode.valueOf(config.getMode().name()));
    }

    @Override
    public String encrypt(String text) {
        try {
            String encryptBcd;
            if (config.isBase64()) {
                encryptBcd = sm2.encryptBase64(text, KeyType.PublicKey);
            } else if (config.isBcd()) {
                encryptBcd = sm2.encryptBcd(text, KeyType.PublicKey);
            } else {
                encryptBcd = sm2.encryptHex(text, KeyType.PublicKey);
            }
            return config.isLowerCase() ? encryptBcd.toLowerCase() : encryptBcd;
        } catch (Exception e) {
            if (!continueOnError) {
                throw e;
            }
        }
        return text;
    }

    @Override
    public String decrypt(String text) {
        try {
            String decryptStr;
            if (config.isBcd()) {
                decryptStr = sm2.decryptStrFromBcd(text, KeyType.PrivateKey);
            } else {
                decryptStr = sm2.decryptStr(text, KeyType.PrivateKey);
            }
            return decryptStr;
        } catch (Exception e) {
            if (!continueOnError) {
                throw e;
            }
        }
        return text;
    }

    public String publicKey() {
        return HexUtil.encodeHexStr(sm2.getQ(false));
    }

    public String privateKey() {
        return sm2.getDHex();
    }

}
