package cn.hiboot.mcn.autoconfigure.web.filter.common;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;


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
    public void customize(JsonMapper.Builder builder) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new StdSerializer<>(String.class) {
            @Override
            public void serialize(String value, JsonGenerator gen, SerializationContext context) throws JacksonException {
                gen.writeString(clean(null, value));
            }
        });
        module.addDeserializer(String.class, new StdDeserializer<>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
                return clean(p.currentName(), p.getString());
            }
        });
        builder.addModule(module);
    }

}
