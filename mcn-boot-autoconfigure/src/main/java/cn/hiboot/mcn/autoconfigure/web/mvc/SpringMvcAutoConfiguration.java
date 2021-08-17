package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.web.exception.handler.AbstractExceptionHandler;
import cn.hiboot.mcn.core.model.ValidationErrorBean;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.swagger.MvcSwagger2;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * spring mvc swagger2 config
 *
 * @author DingHao
 * @since 2019/3/27 10:56
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({DispatcherServlet.class})
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SpringMvcAutoConfiguration {


    @Configuration(proxyBeanMethods = false)
    @Import(GlobalExceptionHandler.class)
    static class SpringMvcExceptionHandler{

        @Bean
        @ConditionalOnMissingBean(ErrorController.class)
        public ErrorPageController errorController() {
            return new ErrorPageController();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ValidationException.class)
    @RestControllerAdvice
    static class ValidationExceptionHandler extends AbstractExceptionHandler {

        @ExceptionHandler(ValidationException.class)
        public RestResp<Object> handleValidationException(ValidationException exception){
            dealStackTraceElement(exception);
            RestResp<Object> objectRestResp = buildErrorMessage(PARAM_PARSE_ERROR);
            if (exception instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) exception;
                objectRestResp.setData(cve.getConstraintViolations().stream().map(violation1 ->
                        new ValidationErrorBean(violation1.getMessage(), getViolationPath(violation1), getViolationInvalidValue(violation1.getInvalidValue()))
                ).collect(Collectors.toList()));
            }
            logger.error("ErrorMsg = {}",objectRestResp.getErrorInfo(),exception);
            return objectRestResp;
        }

        private String getViolationInvalidValue(Object invalidValue) {
            if (invalidValue == null) {
                return null;
            } else {
                if (invalidValue.getClass().isArray()) {
                    if (invalidValue instanceof Object[]) {
                        return Arrays.toString((Object[]) invalidValue);
                    }

                    if (invalidValue instanceof boolean[]) {
                        return Arrays.toString((boolean[]) invalidValue);
                    }

                    if (invalidValue instanceof byte[]) {
                        return Arrays.toString((byte[]) invalidValue);
                    }

                    if (invalidValue instanceof char[]) {
                        return Arrays.toString((char[]) invalidValue);
                    }

                    if (invalidValue instanceof double[]) {
                        return Arrays.toString((double[]) invalidValue);
                    }

                    if (invalidValue instanceof float[]) {
                        return Arrays.toString((float[]) invalidValue);
                    }

                    if (invalidValue instanceof int[]) {
                        return Arrays.toString((int[]) invalidValue);
                    }

                    if (invalidValue instanceof long[]) {
                        return Arrays.toString((long[]) invalidValue);
                    }

                    if (invalidValue instanceof short[]) {
                        return Arrays.toString((short[]) invalidValue);
                    }
                }

                return invalidValue.toString();
            }
        }

        private String getViolationPath(ConstraintViolation violation) {
            String rootBeanName = violation.getRootBean().getClass().getSimpleName();
            String propertyPath = violation.getPropertyPath().toString();
            return rootBeanName + (!"".equals(propertyPath) ? '.' + propertyPath : "");
        }

    }

    @Configuration(proxyBeanMethods = false)
    @EnableSwagger2
    @EnableConfigurationProperties(Swagger2Properties.class)
    @ConditionalOnClass(MvcSwagger2.class)
    @ConditionalOnProperty(prefix = "swagger", name = {"enable"}, havingValue = "true")
    private static class Swagger {

        private final Swagger2Properties swagger2Properties;
        private final ObjectProvider<RequestHandlerPredicate> requestHandlerPredicates;
        private final Predicate<RequestHandler> DEFAULT_REQUEST_HANDLER = withClassAnnotation(RestController.class);

        public Swagger(Swagger2Properties swagger2Properties, ObjectProvider<RequestHandlerPredicate> requestHandlerPredicates) {
            this.swagger2Properties = swagger2Properties;
            this.requestHandlerPredicates = requestHandlerPredicates;
        }

        private Predicate<RequestHandler> withClassAnnotation(Class<? extends Annotation> annotation){
            return RequestHandlerSelectors.withClassAnnotation(annotation);
        }

        @Bean
        public RequestHandlerPredicate requestHandlerPredicate(){
            return () -> Predicates.and(
                        Predicates.not(withClassAnnotation(IgnoreApi.class)),
                        Predicates.not(RequestHandlerSelectors.withMethodAnnotation(IgnoreApi.class)));
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

}
