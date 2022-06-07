package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import cn.hiboot.mcn.core.exception.ServiceException;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
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
@AutoConfiguration
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
        return text -> {
            String s = HtmlUtils.htmlEscape(text);
            if(xssProperties.isFailFast() && !Objects.equals(s,text)){
                throw ServiceException.newInstance("可能存在Xss攻击");
            }
            return s;
        };
    }

    @Bean
    public JacksonXssConfig jacksonXssConfig(XssProcessor xssProcessor) {
        return new JacksonXssConfig(xssProperties.isEscapeResponse(), xssProcessor);
    }

    protected static class JacksonXssConfig implements Jackson2ObjectMapperBuilderCustomizer {

        private final boolean escapeResponse;
        private final XssProcessor xssProcessor;

        public JacksonXssConfig(boolean escapeResponse, XssProcessor xssProcessor) {
            this.escapeResponse = escapeResponse;
            this.xssProcessor = xssProcessor;
        }

        @Override
        public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
            if(escapeResponse){
                jacksonObjectMapperBuilder.serializers(new JsonSerializer<String>(){
                    @Override
                    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                        gen.writeString(xssProcessor.process(value));
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
                    return xssProcessor.process(p.getText());
                }
                @Override
                public Class<String> handledType() {
                    return String.class;
                }
            });
        }
    }

}
