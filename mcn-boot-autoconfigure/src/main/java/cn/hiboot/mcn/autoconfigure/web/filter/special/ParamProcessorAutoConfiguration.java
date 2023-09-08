package cn.hiboot.mcn.autoconfigure.web.filter.special;


import cn.hiboot.mcn.autoconfigure.web.filter.special.reactive.ReactiveParamProcessorConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.special.servlet.ServletParamProcessorConfiguration;
import cn.hiboot.mcn.autoconfigure.web.security.WebSecurityProperties;
import cn.hiboot.mcn.core.exception.ExceptionKeys;
import cn.hiboot.mcn.core.exception.ServiceException;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ParamProcessorAutoConfiguration
 *
 * @author DingHao
 * @since 2022/6/6 15:03
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ParamProcessorProperties.class, WebSecurityProperties.class})
@ConditionalOnProperty(prefix = "param.processor", name = "enabled", havingValue = "true")
@Import({ReactiveParamProcessorConfiguration.class, ServletParamProcessorConfiguration.class})
public class ParamProcessorAutoConfiguration {

    private static final Map<String, Pattern> MAP = new HashMap<>();

    @Bean
    @ConditionalOnMissingBean
    public ParamProcessor defaultParamProcessor(Environment environment) {
        String globalRulePattern = environment.getProperty("global.rule.pattern", "");
        return (rule, name, value) -> {
            String rulePattern = getRule(rule, globalRulePattern);
            if (rulePattern.isEmpty()) {
                return value;
            }
            Pattern pattern = MAP.computeIfAbsent(rulePattern, m -> Pattern.compile(rulePattern));
            if (pattern.matcher(value).matches()) {
                throw ServiceException.newInstance(ExceptionKeys.SPECIAL_SYMBOL_ERROR);
            }
            return value;
        };
    }

    private String getRule(String rule, String globalRulePattern) {
        if (rule.isEmpty()) {
            rule = globalRulePattern;
        }
        return rule;
    }

    public static String getRule(CheckParam classAnnotation, CheckParam methodAnnotation) {
        String methodRule = methodAnnotation != null ? methodAnnotation.value() : "";
        String classRule = classAnnotation != null ? classAnnotation.value() : "";
        if (!methodRule.isEmpty()) {
            return methodRule;
        }
        if (!classRule.isEmpty()) {
            return classRule;
        }
        return "";
    }

    public static Object validStringValue(MethodParameter parameter, Object returnValue, ParamProcessor paramProcessor){
        CheckParam classAnnotation = parameter.getParameterAnnotation(CheckParam.class);
        if (classAnnotation == null) {
            classAnnotation = parameter.getParameterType().getAnnotation(CheckParam.class);
        }
        valid(returnValue,classAnnotation,paramProcessor);
        return returnValue;
    }

    private static void valid(Object value, CheckParam classAnnotation ,ParamProcessor paramProcessor){
        CheckParam usedAnnotation = classAnnotation;
        BeanWrapper src = new BeanWrapperImpl(value);
        for (Field declaredField : value.getClass().getDeclaredFields()) {
            String name = declaredField.getName();
            Object propertyValue = src.getPropertyValue(name);
            if(propertyValue == null){
                continue;
            }
            CheckParam fieldAnnotation = declaredField.getAnnotation(CheckParam.class);
            if (propertyValue instanceof String str) {
                if(usedAnnotation.validString() || fieldAnnotation != null){
                    paramProcessor.process(ParamProcessorAutoConfiguration.getRule(usedAnnotation, fieldAnnotation), name, str);
                }
                continue;
            }
            if((usedAnnotation.validObject() || fieldAnnotation != null) && !BeanUtils.isSimpleProperty(propertyValue.getClass())){
                if(fieldAnnotation != null){
                    valid(propertyValue,fieldAnnotation,paramProcessor);
                    continue;
                }
                CheckParam annotation = propertyValue.getClass().getAnnotation(CheckParam.class);
                if(annotation != null){//成员变量的类型上有注解
                    usedAnnotation = annotation;
                }
                valid(propertyValue,usedAnnotation,paramProcessor);
            }
        }
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static BeanPostProcessor jacksonParamProcessorConfig(ParamProcessor paramProcessor) {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof ObjectMapper mapper) {
                    AnnotationIntrospector sis = mapper.getDeserializationConfig().getAnnotationIntrospector();
                    AnnotationIntrospector pair = AnnotationIntrospectorPair.pair(sis, new ParamProcessorAnnotationIntrospector(paramProcessor));
                    mapper.setAnnotationIntrospector(pair);
                }
                return bean;
            }

        };
    }

    static class ParamProcessorAnnotationIntrospector extends JacksonAnnotationIntrospector {
        private final ParamProcessor paramProcessor;

        public ParamProcessorAnnotationIntrospector(ParamProcessor paramProcessor) {
            this.paramProcessor = paramProcessor;
        }

        @Override
        public Object findDeserializer(Annotated am) {
            if (am instanceof AnnotatedMethod annotatedMethod) {
                if (String.class.isAssignableFrom(annotatedMethod.getParameterType(0).getRawClass())) {
                    CheckParam annotation = am.getAnnotation(CheckParam.class);
                    CheckParam classAnnotation = annotatedMethod.getDeclaringClass().getAnnotation(CheckParam.class);
                    if (annotation == null) {
                        annotation = classAnnotation;
                    }
                    if (annotation != null) {
                        String rule = getRule(classAnnotation, annotation);
                        return new StdDeserializer<String>(String.class) {
                            @Override
                            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                                return paramProcessor.process(rule, p.currentName(), p.getText());
                            }
                        };
                    }
                }
            }
            return null;
        }

    }

}
