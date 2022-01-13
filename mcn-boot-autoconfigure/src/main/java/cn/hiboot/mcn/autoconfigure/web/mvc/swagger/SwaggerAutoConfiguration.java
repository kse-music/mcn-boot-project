package cn.hiboot.mcn.autoconfigure.web.mvc.swagger;

import cn.hiboot.mcn.swagger.MvcSwagger2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.DispatcherServlet;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

/**
 * SwaggerAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/13 22:04
 */
@Configuration(proxyBeanMethods = false)
@EnableSwagger2
@EnableConfigurationProperties(Swagger2Properties.class)
@ConditionalOnClass({DispatcherServlet.class, MvcSwagger2.class})
@ConditionalOnProperty(prefix = "swagger", name = {"enable"}, havingValue = "true")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SwaggerAutoConfiguration {

    private final Swagger2Properties swagger2Properties;
    private final ObjectProvider<RequestHandlerPredicate> requestHandlerPredicates;
    private final Predicate<RequestHandler> DEFAULT_REQUEST_HANDLER = withClassAnnotation(RestController.class);

    public SwaggerAutoConfiguration(Swagger2Properties swagger2Properties, ObjectProvider<RequestHandlerPredicate> requestHandlerPredicates) {
        this.swagger2Properties = swagger2Properties;
        this.requestHandlerPredicates = requestHandlerPredicates;
    }

    private Predicate<RequestHandler> withClassAnnotation(Class<? extends Annotation> annotation){
        return RequestHandlerSelectors.withClassAnnotation(annotation);
    }

    @Bean
    public RequestHandlerPredicate requestHandlerPredicate(){
        return () -> (withClassAnnotation(IgnoreApi.class).negate()).and(RequestHandlerSelectors.withMethodAnnotation(IgnoreApi.class).negate());
    }

    @Bean
    public Docket createRestApi() {
        ApiSelectorBuilder apiSelectorBuilder = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select().apis(DEFAULT_REQUEST_HANDLER);

        for (RequestHandlerPredicate requestHandlerPredicate : requestHandlerPredicates) {
            apiSelectorBuilder.apis(requestHandlerPredicate.get());
        }

        return apiSelectorBuilder.paths(PathSelectors.any()).build().enable(swagger2Properties.isEnable());
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

