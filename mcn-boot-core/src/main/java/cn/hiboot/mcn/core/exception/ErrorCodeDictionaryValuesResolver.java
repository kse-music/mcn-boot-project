package cn.hiboot.mcn.core.exception;

import com.power.common.model.EnumDictionary;
import com.power.doc.extension.dict.DictionaryValuesResolver;
import com.power.doc.model.ApiErrorCode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ErrorCodeDictionaryValuesResolver
 *
 * @author DingHao
 * @since 2023/7/2 18:21
 */

public class ErrorCodeDictionaryValuesResolver implements DictionaryValuesResolver {

    @Nonnull
    @SuppressWarnings("unchecked")
    @Override
    public <T extends EnumDictionary> Collection<T> resolve() {
        List<EnumDictionary> rs = new ArrayList<>();
        ErrorMsg.errMsg.forEach((k, v) -> rs.add(ApiErrorCode.builder().setDesc(v).setValue(k.toString())));
        return (Collection<T>) rs;
    }

}
