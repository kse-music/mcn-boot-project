package cn.hiboot.mcn.cloud.encryptor;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EncryptorProperties
 *
 * @author DingHao
 * @since 2022/2/15 14:10
 */
@ConfigurationProperties("encryptor")
public class EncryptorProperties {

    private SM4 sm4;
    private int order;

    public SM4 getSm4() {
        return sm4;
    }

    public void setSm4(SM4 sm4) {
        this.sm4 = sm4;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public static class SM4{
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
