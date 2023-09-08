package cn.hiboot.mcn.autoconfigure.web.swagger;

import cn.hiboot.mcn.swagger.MvcSwagger2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.RequestHandlerProvider;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
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
@ConditionalOnClass(MvcSwagger2.class)
@ConditionalOnProperty(prefix = "swagger", name = "enabled", havingValue = "true")
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

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RequestHandlerProvider.class)
    protected static class CompatiblePathPatternParser implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
        static final String BEAN_NAME = "webMvcRequestHandlerProvider";

        private Environment environment;

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            if(registry.containsBeanDefinition(BEAN_NAME) && WebMvcProperties.MatchingStrategy.ANT_PATH_MATCHER != environment.getProperty("spring.mvc.pathmatch.matching-strategy", WebMvcProperties.MatchingStrategy.class)){
                registry.removeBeanDefinition(BEAN_NAME);
                registry.registerBeanDefinition(BEAN_NAME,new RootBeanDefinition(WebMvcRequestHandlerProvider.class));
            }
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

    }

    @Bean
    @ConditionalOnMissingBean
    public Docket createRestApi(Environment environment, ObjectProvider<ApiKey> apiKeys) {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).enable(swagger2Properties.isEnable());

        List<ApiKey> apiKeyList = apiKeys.orderedStream().collect(Collectors.toList());
        if(Boolean.TRUE.equals(swagger2Properties.getHeader().getAuthorization()) || (swagger2Properties.getHeader().getAuthorization() == null && ClassUtils.isPresent("org.springframework.security.core.Authentication",null))){
            apiKeyList.add(new ApiKey("JwtToken", "Authorization", "header"));
        }

        configApiKey(docket, apiKeyList);

        docketCustomizers.ifUnique(d -> d.customize(docket));

        configRequestParameters(docket,environment);

        return docket.select().apis(DEFAULT_REQUEST_HANDLER).paths(PathSelectors.any()).build();
    }

    private void configApiKey(Docket docket,List<ApiKey> apiKeys) {
        List<SecurityScheme> apiKeyList = new ArrayList<>();
        List<SecurityReference> securityReferences = new ArrayList<>();
        for (ApiKey apiKey : apiKeys) {
            apiKeyList.add(apiKey);
            securityReferences.add(buildSecurityReference(apiKey));
        }
        SecurityContext securityContext = SecurityContext.builder().securityReferences(securityReferences).operationSelector(p -> true).build();
        docket.securityContexts(Collections.singletonList(securityContext)).securitySchemes(apiKeyList);
    }

    private SecurityReference buildSecurityReference(ApiKey apiKey) {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return new SecurityReference(apiKey.getName(), authorizationScopes);
    }

    private void configRequestParameters(Docket docket,Environment environment) {
        List<RequestParameter> pars = new ArrayList<>();
        //csrf
        if(swagger2Properties.getHeader().isCsrf()){
            pars.add(new RequestParameterBuilder().name("X-XSRF-TOKEN").description("csrf token").in(ParameterType.HEADER).query(s -> s.model(m -> m.scalarModel(ScalarType.STRING))).required(true).build());
        }
        //enable data integrity
        if(environment.getProperty("data.integrity.enabled", Boolean.class, false)){
            pars.add(new RequestParameterBuilder().name("TSM").description("时间戳").in(ParameterType.HEADER).query(s -> s.model(m -> m.scalarModel(ScalarType.LONG))).required(true).build());
            pars.add(new RequestParameterBuilder().name("nonceStr").description("随机字符串").in(ParameterType.HEADER).query(s -> s.model(m -> m.scalarModel(ScalarType.STRING))).required(true).build());
            pars.add(new RequestParameterBuilder().name("signature").description("签名").in(ParameterType.HEADER).query(s -> s.model(m -> m.scalarModel(ScalarType.STRING))).required(true).build());
        }
        docket.globalRequestParameters(pars);
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

