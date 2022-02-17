package cn.hiboot.mcn.cloud.encryptor.sm4;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EncryptorProperties
 *
 * @author DingHao
 * @since 2022/2/15 14:10
 */
@ConfigurationProperties(EncryptorProperties.KEY)
public class EncryptorProperties {

    public static final String KEY = "encryptor";

    private SM4 sm4;

    public SM4 getSm4() {
        return sm4;
    }

    public void setSm4(SM4 sm4) {
        this.sm4 = sm4;
    }

    public static class SM4{
        private boolean base64;
        private String key;

        public boolean isBase64() {
            return base64;
        }

        public void setBase64(boolean base64) {
            this.base64 = base64;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
