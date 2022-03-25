package cn.hiboot.mcn.cloud.encryptor;

import com.fasterxml.jackson.core.JsonParser;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * DecryptDataConverter
 *
 * @author DingHao
 * @since 2022/3/25 10:47
 */
public interface DecryptDataConverter {
    Object apply(JsonParser jp, TextEncryptor textEncryptor) throws Exception;
}
