package cn.hiboot.mcn.autoconfigure.web.filter.common;

import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssProperties;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 处理json中数据类型为string的值
 *
 * @author DingHao
 * @since 2022/6/9 10:47
 */
public class ValueProcessorJacksonConfig implements Jackson2ObjectMapperBuilderCustomizer {
    private static final ThreadLocal<Boolean> feignRequest = ThreadLocal.withInitial(() -> false);

    private boolean escapeResponse;
    private final Map<ValueProcessor, RequestMatcher> valueProcessors;

    public ValueProcessorJacksonConfig(ObjectProvider<ValueProcessor> valueProcessors) {
        this.valueProcessors = valueProcessors.orderedStream().collect(Collectors.toMap(Function.identity(), v -> {
            ValueProcessorProperties properties = null;
            if(v instanceof ParamProcessor){
                properties = SpringBeanUtils.getBean(ParamProcessorProperties.class);
            }else if(v instanceof XssProcessor){
                properties = SpringBeanUtils.getBean(XssProperties.class);
            }
            if(properties == null){
                return RequestMatcher.DEFAULT_REQUEST_MATCHER;
            }
            return new RequestMatcher(properties.getIncludeUrls(), properties.getExcludeUrls());
        }));
    }

    public void setEscapeResponse(boolean escapeResponse) {
        this.escapeResponse = escapeResponse;
    }

    public static void setFeignRequest(){
        feignRequest.set(true);
    }

    public static void removeFeignRequest(){
        feignRequest.remove();
    }

    private String clean(String name, String text){
        if(feignRequest.get()){//don't deal feign request
            return text;
        }
        for (Map.Entry<ValueProcessor, RequestMatcher> entry : valueProcessors.entrySet()) {
            HttpServletRequest httpRequest = getHttpRequest();
            if(httpRequest == null || entry.getValue().matches(httpRequest)){
                text = entry.getKey().process(name,text);
            }
        }
        return text;
    }

    private HttpServletRequest getHttpRequest(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null){
            return null;
        }
        return requestAttributes.getRequest();
    }

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        if(escapeResponse){
            jacksonObjectMapperBuilder.serializers(new JsonSerializer<String>(){
                @Override
                public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeString(clean(null,value));
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
                return clean(p.currentName(),p.getText());
            }
            @Override
            public Class<String> handledType() {
                return String.class;
            }
        });
    }
}
