package cn.hiboot.mcn.autoconfigure.web.filter.xss;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> filterRegistrationBean = new FilterRegistrationBean<>(new XssFilter(xssProperties));
        filterRegistrationBean.setOrder(xssProperties.getOrder());
        filterRegistrationBean.addUrlPatterns(xssProperties.getUrlPatterns());
        return filterRegistrationBean;
    }

    @Bean
    public JacksonXssConfig jacksonXssConfig() {
        return new JacksonXssConfig(xssProperties.isEscapeResponse());
    }

    protected static class JacksonXssConfig implements Jackson2ObjectMapperBuilderCustomizer {

        private final boolean escapeResponse;

        public JacksonXssConfig(boolean escapeResponse) {
            this.escapeResponse = escapeResponse;
        }

        @Override
        public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
            SimpleModule simpleModule = new SimpleModule();
            if(escapeResponse){
                simpleModule.addSerializer(String.class, new JsonSerializer<String>(){
                    @Override
                    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                        gen.writeString(HtmlUtils.htmlEscape(value));
                    }
                });
            }
            simpleModule.addDeserializer(String.class, new JsonDeserializer<String>(){
                @Override
                public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                    return HtmlUtils.htmlEscape(p.getText());
                }
            });
            jacksonObjectMapperBuilder.modules(simpleModule);
        }
    }

}
