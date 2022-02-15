package cn.hiboot.mcn.cloud.encryptor;

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
        private boolean enable;
        private String key;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
