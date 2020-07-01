package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.autoconfigure.web.exception.handler.AbstractExceptionHandler;
import cn.hiboot.mcn.core.model.ValidationErrorBean;
import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.swagger.MvcSwagger2;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        public ErrorController errorController() {
            return new ErrorPageController();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ValidationException.class)
    @RestControllerAdvice
    static class ValidationExceptionHandler extends AbstractExceptionHandler {

        @ExceptionHandler(ValidationException.class)
        public RestResp handleValidationException(ValidationException exception){
            dealStackTraceElement(exception);
            RestResp<List<ValidationErrorBean>> objectRestResp = buildErrorMessage(PARAM_PARSE_ERROR);
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
                        return Arrays.toString((Object[])((Object[])invalidValue));
                    }

                    if (invalidValue instanceof boolean[]) {
                        return Arrays.toString((boolean[])((boolean[])invalidValue));
                    }

                    if (invalidValue instanceof byte[]) {
                        return Arrays.toString((byte[])((byte[])invalidValue));
                    }

                    if (invalidValue instanceof char[]) {
                        return Arrays.toString((char[])((char[])invalidValue));
                    }

                    if (invalidValue instanceof double[]) {
                        return Arrays.toString((double[])((double[])invalidValue));
                    }

                    if (invalidValue instanceof float[]) {
                        return Arrays.toString((float[])((float[])invalidValue));
                    }

                    if (invalidValue instanceof int[]) {
                        return Arrays.toString((int[])((int[])invalidValue));
                    }

                    if (invalidValue instanceof long[]) {
                        return Arrays.toString((long[])((long[])invalidValue));
                    }

                    if (invalidValue instanceof short[]) {
                        return Arrays.toString((short[])((short[])invalidValue));
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
    @ConditionalOnClass({MvcSwagger2.class})
    @ConditionalOnProperty(prefix = "swagger", name = {"enable"}, havingValue = "true")
    private static class Swagger {

        @Value("${app.base-package}")
        private String pkg;

        private Swagger2Properties swagger2Properties;
        private ObjectProvider<DocketCustomizer> customizers;

        public Swagger(Swagger2Properties swagger2Properties, ObjectProvider<DocketCustomizer> customizers) {
            this.swagger2Properties = swagger2Properties;
            this.customizers = customizers;
        }

        @Bean
        public Docket createRestApi() {
            List<String> packages = swagger2Properties.getPackages();

            if(packages == null){
                packages = new ArrayList<>();
            }

            packages.add(pkg + ".rest");

            Docket docket = new Docket(DocumentationType.SWAGGER_2)
                    .apiInfo(apiInfo())
                    .select()
                    .apis(Predicates.or(packages.stream().map(RequestHandlerSelectors::basePackage).toArray(Predicate[]::new)))
                    .paths(PathSelectors.any())
                    .build().enable(swagger2Properties.isEnable());
            this.customizers.orderedStream().forEach((customizer) -> {
                customizer.customize(docket);
            });
            return docket;
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
