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

    private boolean continueOnError;

    private SM2 sm2;

    private SM4 sm4;

    public boolean isContinueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    public SM2 getSm2() {
        return sm2;
    }

    public void setSm2(SM2 sm2) {
        this.sm2 = sm2;
    }

    public SM4 getSm4() {
        return sm4;
    }

    public void setSm4(SM4 sm4) {
        this.sm4 = sm4;
    }

    public static class SM2{
        private boolean base64;
        private boolean lowerCase;
        private String privateKey;
        private String publicKey;
        private Mode mode = Mode.C1C3C2;

        public boolean isBase64() {
            return base64;
        }

        public void setBase64(boolean base64) {
            this.base64 = base64;
        }

        public boolean isLowerCase() {
            return lowerCase;
        }

        public void setLowerCase(boolean lowerCase) {
            this.lowerCase = lowerCase;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        public enum Mode {
            C1C2C3,
            C1C3C2;
        }
    }

    public static class SM4{
        private boolean base64;
        private Mode mode;
        private Padding padding;
        private String key;
        private String iv;
        private boolean useDefault = true;

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

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public boolean isUseDefault() {
            return useDefault;
        }

        public void setUseDefault(boolean useDefault) {
            this.useDefault = useDefault;
        }

        public enum Mode {
            NONE,
            CBC,
            CFB,
            CTR,
            CTS,
            ECB,
            OFB,
            PCBC;
        }

        public enum Padding {
            NoPadding,
            ZeroPadding,
            ISO10126Padding,
            OAEPPadding,
            PKCS1Padding,
            PKCS5Padding,
            SSL3Padding;
        }

    }
}
