package cn.hiboot.mcn.autoconfigure.web.filter.special;


import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessorJacksonConfig;
import cn.hiboot.mcn.core.exception.ServiceException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ParamProcessorAutoConfiguration
 *
 * @author DingHao
 * @since 2022/6/6 15:03
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ParamProcessorProperties.class)
@ConditionalOnProperty(prefix = "param.processor",name = "enable",havingValue = "true")
public class ParamProcessorAutoConfiguration {

    private static final Map<String,Pattern> MAP = new HashMap<>();

    private final ParamProcessorProperties properties;

    public ParamProcessorAutoConfiguration(ParamProcessorProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ParamProcessor defaultParamProcessor(Environment environment) {
        String globalRulePattern = environment.getProperty("global.rule.pattern","");
        return (rule,value) -> {
            String rulePattern = getRule(rule,globalRulePattern);
            if(rulePattern.isEmpty()){
                return value;
            }
            Pattern pattern = MAP.computeIfAbsent(rule, m -> Pattern.compile(rulePattern));
            if(pattern.matcher(value).matches()){
                throw ServiceException.newInstance("存在特殊字符");
            }
            return value;
        };
    }

    private String getRule(String rule,String globalRulePattern){
        if(rule.isEmpty()){
            rule = globalRulePattern;
        }
        return rule;
    }

    @Bean
    public FilterRegistrationBean<ParamProcessorFilter> paramProcessorFilterRegistration(ParamProcessor paramProcessor) {
        FilterRegistrationBean<ParamProcessorFilter> filterRegistrationBean = new FilterRegistrationBean<>(new ParamProcessorFilter(properties,paramProcessor));
        filterRegistrationBean.setOrder(properties.getOrder());
        return filterRegistrationBean;
    }

    @Bean
    public ValueProcessorJacksonConfig paramProcessorJacksonConfig(ParamProcessor paramProcessor) {
        return new ValueProcessorJacksonConfig(paramProcessor);
    }

    @Bean
    public WebMvcConfigurer WebMvcConfig(ParamProcessor paramProcessor) {

        return new WebMvcConfigurer(){
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new KeyValueArgumentResolver(paramProcessor));
                resolvers.add(new HandlerMethodArgumentResolver(){

                    private final ServletModelAttributeMethodProcessor processor = new ServletModelAttributeMethodProcessor(true);

                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        CheckParam classAnnotation = parameter.getParameterType().getAnnotation(CheckParam.class);
                        return processor.supportsParameter(parameter) && (parameter.hasParameterAnnotation(CheckParam.class) || classAnnotation != null);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                        Object returnValue = processor.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
                        if(returnValue == null){
                            return null;
                        }
                        CheckParam classAnnotation = parameter.getParameterType().getAnnotation(CheckParam.class);
                        if(classAnnotation == null){
                            classAnnotation = parameter.getParameterAnnotation(CheckParam.class);
                        }
                        BeanWrapper src = new BeanWrapperImpl(returnValue);
                        for (Field declaredField : returnValue.getClass().getDeclaredFields()) {
                            Object propertyValue = src.getPropertyValue(declaredField.getName());
                            if(propertyValue instanceof String){
                                paramProcessor.process(getRule(classAnnotation,declaredField.getAnnotation(CheckParam.class)),propertyValue.toString());
                            }
                        }
                        return returnValue;
                    }


                });
            }
        };

    }

    static String getRule(CheckParam classAnnotation , CheckParam methodAnnotation){
        String methodRule = methodAnnotation != null ? methodAnnotation.value() : "";
        String classRule = classAnnotation != null ? classAnnotation.value() : "";
        if(!methodRule.isEmpty()){
            return methodRule;
        }
        if(!classRule.isEmpty()){
            return classRule;
        }
        return "";
    }

    protected static class KeyValueArgumentResolver implements HandlerMethodArgumentResolver {

        private final ParamProcessor paramProcessor;

        public KeyValueArgumentResolver(ParamProcessor paramProcessor) {
            this.paramProcessor = paramProcessor;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CheckParam.class) && parameter.getParameterType() == String.class;
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if(request == null){
                return null;
            }
            String rule = parameter.getParameterAnnotation(CheckParam.class).value();
            return paramProcessor.process(rule,request.getParameter(parameter.getParameterName()));
        }

    }

    @Bean
    @Role(2)
    public static BeanPostProcessor jacksonParamProcessorConfig(ParamProcessor paramProcessor, Environment environment) {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if(bean instanceof ObjectMapper){
                    ObjectMapper mapper = (ObjectMapper) bean;
                    AnnotationIntrospector sis = mapper.getDeserializationConfig().getAnnotationIntrospector();
                    AnnotationIntrospector pair = AnnotationIntrospectorPair.pair(sis, new ParamProcessorAnnotationIntrospector(paramProcessor));
                    mapper.setAnnotationIntrospector(pair);
                }
                return bean;
            }

        };
    }

    protected static class ParamProcessorAnnotationIntrospector extends JacksonAnnotationIntrospector {
        private final ParamProcessor paramProcessor;

        public ParamProcessorAnnotationIntrospector(ParamProcessor paramProcessor) {
            this.paramProcessor = paramProcessor;
        }

        @Override
        public Object findDeserializer(Annotated am) {
            if(am instanceof AnnotatedMethod){
                AnnotatedMethod annotatedMethod = (AnnotatedMethod) am;
                if(String.class.isAssignableFrom(annotatedMethod.getParameterType(0).getRawClass())){
                    CheckParam annotation = am.getAnnotation(CheckParam.class);
                    CheckParam classAnnotation = annotatedMethod.getDeclaringClass().getAnnotation(CheckParam.class);
                    if(annotation == null){
                        annotation = classAnnotation;
                    }
                    if(annotation != null){
                        String rule = getRule(classAnnotation,annotation);
                        return new StdDeserializer<String>(String.class){
                            @Override
                            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                                return paramProcessor.process(rule,p.getText());
                            }
                        };
                    }
                }
            }
            return null;
        }

    }

}
