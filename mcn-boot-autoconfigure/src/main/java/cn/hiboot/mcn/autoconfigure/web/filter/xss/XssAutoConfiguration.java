package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import cn.hiboot.mcn.core.exception.ServiceException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * XssAutoConfiguration
 *
 * @author DingHao
 * @since 2022/6/6 10:10
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "mcn.xss", name = "enable", havingValue = "true")
@EnableConfigurationProperties(XssProperties.class)
public class XssAutoConfiguration {

    private final XssProperties xssProperties;

    public XssAutoConfiguration(XssProperties xssProperties) {
        this.xssProperties = xssProperties;
    }

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration(XssProcessor xssProcessor) {
        FilterRegistrationBean<XssFilter> filterRegistrationBean = new FilterRegistrationBean<>(new XssFilter(xssProperties,xssProcessor));
        filterRegistrationBean.setOrder(xssProperties.getOrder());
        filterRegistrationBean.addUrlPatterns(xssProperties.getUrlPatterns());
        return filterRegistrationBean;
    }

    @Bean
    @ConditionalOnMissingBean(XssProcessor.class)
    public XssProcessor defaultXssProcessor(){
        return HtmlUtils::htmlEscape;
    }

    @Bean
    public JacksonXssConfig jacksonXssConfig(XssProcessor xssProcessor) {
        return new JacksonXssConfig(xssProperties, xssProcessor);
    }

    protected static class JacksonXssConfig implements Jackson2ObjectMapperBuilderCustomizer {

        private final XssProperties xssProperties;
        private final XssProcessor xssProcessor;

        public JacksonXssConfig(XssProperties xssProperties, XssProcessor xssProcessor) {
            this.xssProperties = xssProperties;
            this.xssProcessor = xssProcessor;
        }

        private String clean(String text){
            String result = xssProcessor.process(text);
            if(xssProperties.isFailFast() && !Objects.equals(result,text)){
                throw ServiceException.newInstance("可能存在Xss攻击");
            }
            return result;
        }

        @Override
        public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
            if(xssProperties.isEscapeResponse()){
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

}
