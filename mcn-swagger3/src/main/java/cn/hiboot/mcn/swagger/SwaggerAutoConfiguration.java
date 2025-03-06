package cn.hiboot.mcn.swagger;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ClassUtils;

import java.util.Collections;

/**
 * SwaggerAutoConfiguration
 *
 * @author DingHao
 * @since 2022/1/13 22:04
 */
@AutoConfiguration
@Import(Swagger3Properties.class)
public class SwaggerAutoConfiguration {

    private final Swagger3Properties swagger2Properties;

    public SwaggerAutoConfiguration(Swagger3Properties swagger2Properties) {
        this.swagger2Properties = swagger2Properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openApi(Environment environment) {
        return new OpenAPI()
                .components(components(environment))
                .info(new Info().title(swagger2Properties.getTitle())
                        .contact(new Contact().name(swagger2Properties.getName()).email(swagger2Properties.getEmail()).url(swagger2Properties.getUrl()))
                        .version(swagger2Properties.getVersion())
                        .description(swagger2Properties.getDescription()))
                .security(Collections.singletonList(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION)));
    }

    @Bean
    GlobalOpenApiCustomizer mcnGlobalOpenApiCustomizer(Environment environment) {
        return openApi -> openApi.getPaths().values().stream().flatMap(pathItem -> pathItem.readOperations().stream())
                .forEach(operation -> {
                    operation.security(openApi.getSecurity());
                    if(swagger2Properties.getHeader().isCsrf()){
                        operation.addParametersItem(new HeaderParameter().$ref("#/components/parameters/csrf"));
                    }
                    if(environment.getProperty("data.integrity.enabled", Boolean.class, false)){
                        operation.addParametersItem(new HeaderParameter().$ref("#/components/parameters/tsm"))
                                .addParametersItem(new HeaderParameter().$ref("#/components/parameters/nonce"))
                                .addParametersItem(new HeaderParameter().$ref("#/components/parameters/sign"));
                    }
                });
    }

    private Components components(Environment environment) {
        Components components = new Components();
        if(Boolean.TRUE.equals(swagger2Properties.getHeader().getAuthorization()) || (swagger2Properties.getHeader().getAuthorization() == null && ClassUtils.isPresent("org.springframework.security.core.Authentication",null))){
            components.addSecuritySchemes(HttpHeaders.AUTHORIZATION,new SecurityScheme().scheme("bearer").bearerFormat("JWT").type(SecurityScheme.Type.HTTP));
        }
        //csrf
        if(swagger2Properties.getHeader().isCsrf()){
            components.addParameters("csrf",new HeaderParameter().name("X-XSRF-TOKEN").description("csrf token").in(ParameterIn.HEADER.toString()).schema(new StringSchema()).required(true));
        }
        //enable data integrity
        if(environment.getProperty("data.integrity.enabled", Boolean.class, false)){
            components.addParameters("tsm",new HeaderParameter().name("TSM").description("时间戳").in(ParameterIn.HEADER.toString()).schema(new StringSchema()).required(true));
            components.addParameters("nonce",new HeaderParameter().name("nonceStr").description("随机字符串").in(ParameterIn.HEADER.toString()).schema(new StringSchema()).required(true));
            components.addParameters("sign",new HeaderParameter().name("signature").description("签名").in(ParameterIn.HEADER.toString()).schema(new StringSchema()).required(true));
        }
        return components;
    }

}

