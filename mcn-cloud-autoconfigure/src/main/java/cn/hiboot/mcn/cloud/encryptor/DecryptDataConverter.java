package cn.hiboot.mcn.cloud.encryptor;

import cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

/**
 * DecryptDataConverter
 *
 * @author DingHao
 * @since 2022/3/25 10:47
 */
public interface DecryptDataConverter {
    Object apply(JsonParser jp, TextEncryptor textEncryptor) throws IOException;
}
