package cn.hiboot.mcn.autoconfigure.web.mvc;

import cn.hiboot.mcn.swagger.MvcSwagger2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

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
    @Import({GlobalExceptionHandler.class,ErrorPageController.class})
    static class SpringMvcExceptionHandler{

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
            Docket docket =  new Docket(DocumentationType.SWAGGER_2)
                    .apiInfo(apiInfo())
                    .select()
                    .apis(RequestHandlerSelectors.basePackage(pkg + ".rest"))
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


//        @Configuration
//        public static class Swagger2Mapping extends WebMvcConfigurationSupport {
//            @Override
//            protected void addResourceHandlers(ResourceHandlerRegistry registry) {
//                registry.addResourceHandler("/swagger-ui.html")
//                        .addResourceLocations("classpath:/META-INF/resources/","/static", "/public");
//
//                registry.addResourceHandler("/webjars/**")
//                        .addResourceLocations("classpath:/META-INF/resources/webjars/");
//
//                super.addResourceHandlers(registry);
//            }
//        }
    }

}
