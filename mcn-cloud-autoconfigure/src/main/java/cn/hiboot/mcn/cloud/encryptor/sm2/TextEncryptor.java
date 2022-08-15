package cn.hiboot.mcn.cloud.encryptor.sm2;

/**
 * TextEncryptor
 *
 * @author DingHao
 * @since 2022/8/11 10:21
 */
public interface TextEncryptor {
    /**
     * 公钥加密
     *
     * @param text 文本
     * @return 加密串
     */
    String encrypt(String text);

    /**
     * 私钥解密
     *
     * @param encryptedText 文本
     * @return 加密串
     */
    String decrypt(String encryptedText);

    String publicKey();
    String privateKey();

}