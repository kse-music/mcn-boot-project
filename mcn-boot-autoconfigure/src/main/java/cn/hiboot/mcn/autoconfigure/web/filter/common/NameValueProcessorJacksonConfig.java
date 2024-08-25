package cn.hiboot.mcn.autoconfigure.web.filter.common;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;


/**
 * 处理json中数据类型为string的值
 *
 * @author DingHao
 * @since 2022/6/9 10:47
 */
public class NameValueProcessorJacksonConfig implements Jackson2ObjectMapperBuilderCustomizer {

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
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        jacksonObjectMapperBuilder.serializers(new StdSerializer<>(String.class) {
            @Override
            public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(clean(null, value));
            }
        });
        jacksonObjectMapperBuilder.deserializers(new StdDeserializer<>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                return clean(p.currentName(), p.getText());
            }
        });
    }

}
