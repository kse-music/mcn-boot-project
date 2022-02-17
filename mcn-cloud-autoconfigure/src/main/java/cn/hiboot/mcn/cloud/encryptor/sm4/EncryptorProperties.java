package cn.hiboot.mcn.cloud.encryptor.sm4;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
        @NestedConfigurationProperty
        private Mode mode;
        @NestedConfigurationProperty
        private Padding padding;
        private String key;

        public boolean isBase64() {
            return base64;
        }

        public void setBase64(boolean base64) {
            this.base64 = base64;
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        public Padding getPadding() {
            return padding;
        }

        public void setPadding(Padding padding) {
            this.padding = padding;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
