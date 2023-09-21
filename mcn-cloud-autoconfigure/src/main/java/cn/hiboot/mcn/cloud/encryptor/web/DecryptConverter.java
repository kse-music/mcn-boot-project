package cn.hiboot.mcn.cloud.encryptor.web;

import cn.hiboot.mcn.cloud.encryptor.Decrypt;
import cn.hiboot.mcn.cloud.encryptor.sm2.TextEncryptor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import java.util.HashSet;
import java.util.Set;

/**
 * Decrypt convert encrypt String to String or Number for key-value encode
 * adapt java bean data bind
 *
 * @author DingHao
 * @since 2022/2/17 11:47
 */
class DecryptConverter implements ConditionalGenericConverter {

    private final TextEncryptor textEncryptor;
    private final ConversionService conversionService;

    public DecryptConverter(TextEncryptor textEncryptor,ConversionService conversionService) {
        this.textEncryptor = textEncryptor;
        this.conversionService = conversionService;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.hasAnnotation(Decrypt.class);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> set = new HashSet<>();
        set.add(new ConvertiblePair(String.class, String.class));
        set.add(new ConvertiblePair(String.class, Number.class));
        return set;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if(source == null || source.toString().isEmpty()){
            return null;
        }
        String decrypt = textEncryptor.decrypt(source.toString());
        if(Number.class.isAssignableFrom(targetType.getObjectType())){
            return conversionService.convert(decrypt,targetType.getObjectType());
        }
        return decrypt;
    }

}
