package cn.hiboot.mcn.autoconfigure.web.swagger;

import cn.hiboot.mcn.swagger.MvcSwagger2;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.DispatcherServlet;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final ObjectProvider<DocketCustomizer> docketCustomizers;

    public SwaggerAutoConfiguration(Swagger2Properties swagger2Properties, ObjectProvider<DocketCustomizer> docketCustomizers) {
        this.swagger2Properties = swagger2Properties;
        this.docketCustomizers = docketCustomizers;
    }

    private Predicate<RequestHandler> selector(Class<? extends Annotation> annotation){
        return Predicates.and(RequestHandlerSelectors.withClassAnnotation(RestController.class),Predicates.not(RequestHandlerSelectors.withClassAnnotation(annotation))
                ,Predicates.not(RequestHandlerSelectors.withMethodAnnotation(annotation)));
    }

    @Bean
    @ConditionalOnMissingBean
    public Docket createRestApi(Environment environment) {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).enable(swagger2Properties.isEnable());

        docketCustomizers.ifUnique(d -> d.customize(docket));

        configRequestParameters(docket,environment);

        for (DocketCustomizer docketCustomizer : docketCustomizers) {
            docketCustomizer.customize(docket);
        }

        ApiSelectorBuilder apiSelectorBuilder = docket.select().apis(selector(IgnoreApi.class));

        return apiSelectorBuilder.paths(PathSelectors.any()).build();
    }

    private void configRequestParameters(Docket docket,Environment environment) {
        docket.securitySchemes(Collections.singletonList(new ApiKey("BearerToken", "Authorization", "header")));
        List<Parameter> pars = new ArrayList<>();
        //csrf
        if(swagger2Properties.isCsrf()){
            pars.add(new ParameterBuilder().name("X-XSRF-TOKEN").description("csrf token").modelRef(new ModelRef("string")).parameterType("header").required(true).build());
        }
        //enable data integrity
        if(environment.getProperty("data.integrity.enable", Boolean.class, false)){
            pars.add(new ParameterBuilder().name("TSM").description("时间戳").parameterType("header").modelRef(new ModelRef("long")).required(true).build());
            pars.add(new ParameterBuilder().name("nonceStr").description("随机字符串").parameterType("header").modelRef(new ModelRef("string")).required(true).build());
            pars.add(new ParameterBuilder().name("signature").description("签名").parameterType("header").modelRef(new ModelRef("string")).required(true).build());
        }
        docket.globalOperationParameters(pars);
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

