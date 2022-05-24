package cn.hiboot.mcn.autoconfigure.web.swagger;

import cn.hiboot.mcn.swagger.MvcSwagger2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * SwaggerAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/13 22:04
 */
@AutoConfiguration
@EnableSwagger2
@EnableConfigurationProperties(Swagger2Properties.class)
@ConditionalOnClass({DispatcherServlet.class, MvcSwagger2.class})
@ConditionalOnProperty(prefix = "swagger", name = {"enable"}, havingValue = "true")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SwaggerAutoConfiguration {

    private final Swagger2Properties swagger2Properties;
    private final ObjectProvider<DocketCustomizer> docketCustomizers;
    private final Predicate<RequestHandler> DEFAULT_REQUEST_HANDLER = withClassAnnotation(RestController.class)
            .and(withClassAnnotation(IgnoreApi.class).negate()).and(RequestHandlerSelectors.withMethodAnnotation(IgnoreApi.class).negate());

    public SwaggerAutoConfiguration(Swagger2Properties swagger2Properties, ObjectProvider<DocketCustomizer> docketCustomizers) {
        this.swagger2Properties = swagger2Properties;
        this.docketCustomizers = docketCustomizers;
    }

    private Predicate<RequestHandler> withClassAnnotation(Class<? extends Annotation> annotation){
        return RequestHandlerSelectors.withClassAnnotation(annotation);
    }

    @Bean
    static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor(){
        //for compatible spring boot 2.6+
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                List<T> copy = mappings.stream()
                        .filter(mapping -> mapping.getPatternParser() == null)
                        .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    ReflectionUtils.makeAccessible(field);
                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public Docket createRestApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).enable(swagger2Properties.isEnable());

        docketCustomizers.ifUnique(d -> d.customize(docket));

        for (DocketCustomizer docketCustomizer : docketCustomizers) {
            docketCustomizer.customize(docket);
        }

        ApiSelectorBuilder apiSelectorBuilder = docket.select().apis(DEFAULT_REQUEST_HANDLER);

        return apiSelectorBuilder.paths(PathSelectors.any()).build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(swagger2Properties.getTitle())
                .description(swagger2Properties.getDescription())
                .termsOfServiceUrl(swagger2Properties.getTermsOfServiceUrl())
                .contact(new Contact(swagger2Properties.getName(),swagger2Properties.getUrl(),swagger2Properties.getEmail()))
                .version(swagger2Properties.getVersion())
                .build();
    }

}

