package cn.hiboot.mcn.autoconfigure.web.filter.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JacksonXssConfig
 *
 * @author DingHao
 * @since 2022/6/9 10:47
 */
public class ValueProcessorJacksonConfig implements Jackson2ObjectMapperBuilderCustomizer {

    private final boolean escapeResponse;
    private final List<ValueProcessor> valueProcessors;

    public ValueProcessorJacksonConfig(ObjectProvider<ValueProcessor> valueProcessors) {
        this(false,valueProcessors);
    }

    public ValueProcessorJacksonConfig(boolean escapeResponse, ObjectProvider<ValueProcessor> valueProcessors) {
        this.escapeResponse = escapeResponse;
        this.valueProcessors = valueProcessors.orderedStream().collect(Collectors.toList());
    }

    private String clean(String text){
        for (ValueProcessor valueProcessor : valueProcessors) {
            text = valueProcessor.process(text);
        }
        return text;
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
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return clean(p.getText());
            }
            @Override
            public Class<String> handledType() {
                return String.class;
            }
        });
    }
}
