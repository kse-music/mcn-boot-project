package cn.hiboot.mcn.autoconfigure.web.filter.common;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;


/**
 * 处理json中数据类型为string的值
 *
 * @author DingHao
 * @since 2022/6/9 10:47
 */
public class NameValueProcessorJacksonConfig implements JsonMapperBuilderCustomizer {

    private static final ThreadLocal<Boolean> feignRequest = ThreadLocal.withInitial(() -> false);

    private final DelegateNameValueProcessor delegateValueProcessor;

    public NameValueProcessorJacksonConfig(ObjectProvider<NameValueProcessor> valueProcessors) {
        this.delegateValueProcessor = new DelegateNameValueProcessor(valueProcessors);
    }

    public static void setFeignRequest() {
        feignRequest.set(true);
    }

    public static void removeFeignRequest() {
        feignRequest.remove();
    }

    private String clean(String name, String text) {
        if (feignRequest.get()) {//don't deal feign request
            return text;
        }
        return delegateValueProcessor.process(name, text);
    }

    @Override
    public void customize(JsonMapper.Builder jsonMapperBuilder) {
        jsonMapperBuilder.addModule(new SimpleModule().addSerializer(String.class, new ValueSerializer<>() {

            @Override
            public void serialize(String value, tools.jackson.core.JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
                gen.writeString(clean(null, value));
            }

        }).addDeserializer(String.class, new ValueDeserializer<>() {

            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
                return clean(p.currentName(), p.getText());
            }

        }));
    }
}
