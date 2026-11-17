package cn.hiboot.mcn.docs;

import cn.hiboot.mcn.cloud.encryptor.Decrypt;
import cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EncryptionExample {
    private final TextEncryptor textEncryptor;

    public EncryptionExample(TextEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

    @GetMapping("/decrypt-demo")
    public RestResp<String> read(@Decrypt String value) {
        return RestResp.ok(value);
    }

    public String encrypt(String plaintext) {
        return textEncryptor.encrypt(plaintext);
    }
}
