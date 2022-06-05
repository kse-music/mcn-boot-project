package cn.hiboot.mcn.autoconfigure.web.filter;

import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssFilter;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssProperties;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

/**
 * uniform register some filter
 *
 * @author DingHao
 * @since 2019/1/9 11:31
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({XssProperties.class, CorsProperties.class})
public class FilterAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "filter", name = "cross", havingValue = "true")
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsProperties corsProperties,CorsConfigurationSource corsConfigurationSource) {
        FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
        filterRegistrationBean.setOrder(corsProperties.getOrder());
        return filterRegistrationBean;
    }

    @Bean
    @ConditionalOnMissingBean(name = "corsConfigurationSource")
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(corsProperties.getAllowCredentials());
        corsConfiguration.addAllowedOrigin(corsProperties.getAllowedOrigin());
        corsConfiguration.addAllowedHeader(corsProperties.getAllowedHeader());
        corsConfiguration.addAllowedMethod(corsProperties.getAllowedMethod());
        corsConfiguration.setMaxAge(corsProperties.getMaxAge());
        source.registerCorsConfiguration(corsProperties.getPattern(), corsConfiguration);
        return source;
    }

    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public DurationAop durationAop() {
        return new DurationAop();
    }

    @Bean
    @ConditionalOnProperty(prefix = "mcn.xss", name = "enable", havingValue = "true")
    public FilterRegistrationBean<XssFilter> xssFilterRegistration(XssProperties xssProperties) {
        FilterRegistrationBean<XssFilter> filterRegistrationBean = new FilterRegistrationBean<>(new XssFilter(xssProperties));
        filterRegistrationBean.setOrder(xssProperties.getOrder());
        filterRegistrationBean.addUrlPatterns(xssProperties.getUrlPatterns());
        return filterRegistrationBean;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "mcn.xss", name = "enable", havingValue = "true")
    protected static class JacksonXssConfig implements Jackson2ObjectMapperBuilderCustomizer {
        @Override
        public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(String.class, new JsonSerializer<String>(){
                @Override
                public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeString(HtmlUtils.htmlEscape(value));
                }
            });
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
