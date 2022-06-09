package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessor;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

/**
 * JacksonXssConfig
 *
 * @author DingHao
 * @since 2022/6/9 10:47
 */
public class JacksonXssConfig implements Jackson2ObjectMapperBuilderCustomizer {

    private final boolean escapeResponse;
    private final ValueProcessor valueProcessor;

    public JacksonXssConfig(ValueProcessor valueProcessor) {
        this(false,valueProcessor);
    }

    public JacksonXssConfig(boolean escapeResponse, ValueProcessor valueProcessor) {
        this.escapeResponse = escapeResponse;
        this.valueProcessor = valueProcessor;
    }

    private String clean(String text){
        return valueProcessor.process(text);
    }

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        if(escapeResponse){
            jacksonObjectMapperBuilder.serializers(new JsonSerializer<String>(){
                @Override
                public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeString(clean(value));
                }
                @Override
                public Class<String> handledType() {
                    return String.class;
                }
            });
        }
        jacksonObjectMapperBuilder.deserializers( new JsonDeserializer<String>(){
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                return clean(p.getText());
            }
            @Override
            public Class<String> handledType() {
                return String.class;
            }
        });
    }
}
