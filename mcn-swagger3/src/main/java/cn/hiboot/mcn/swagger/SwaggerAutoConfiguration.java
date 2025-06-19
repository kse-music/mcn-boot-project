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
import io.swagger.v3.oas.models.servers.Server;
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

    private final Swagger3Properties swaggerProperties;

    public SwaggerAutoConfiguration(Swagger3Properties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openApi(Environment environment) {
        return new OpenAPI()
                .components(components(environment))
                .info(new Info().title(swaggerProperties.getTitle())
                        .contact(new Contact().name(swaggerProperties.getName()).email(swaggerProperties.getEmail()).url(swaggerProperties.getUrl()))
                        .version(swaggerProperties.getVersion())
                        .description(swaggerProperties.getDescription()))
                .security(Collections.singletonList(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION)));
    }


    @Bean
    GlobalOpenApiCustomizer mcnGlobalOpenApiCustomizer(Environment environment) {
        return openApi -> {
            Swagger3Properties.Server server = swaggerProperties.getServer();
            if (server != null) {
                Server serversItem = new Server();
                serversItem.setUrl(server.getUrl());
                serversItem.setDescription(server.getDescription());
                openApi.setServers(Collections.singletonList(serversItem));
            }
            openApi.getPaths().values().stream().flatMap(pathItem -> pathItem.readOperations().stream())
                    .forEach(operation -> {
                        operation.security(openApi.getSecurity());
                        if(swaggerProperties.getHeader().isCsrf()){
                            operation.addParametersItem(new HeaderParameter().$ref("#/components/parameters/csrf"));
                        }
                        if(environment.getProperty("data.integrity.enabled", Boolean.class, false)){
                            operation.addParametersItem(new HeaderParameter().$ref("#/components/parameters/tsm"))
                                    .addParametersItem(new HeaderParameter().$ref("#/components/parameters/nonce"))
                                    .addParametersItem(new HeaderParameter().$ref("#/components/parameters/sign"));
                        }
                    });
        };
    }

    private Components components(Environment environment) {
        Components components = new Components();
        Swagger3Properties.Header header = swaggerProperties.getHeader();
        if(Boolean.TRUE.equals(header.getAuthorization()) || (header.getAuthorization() == null && ClassUtils.isPresent("org.springframework.security.core.Authentication",null))){
            components.addSecuritySchemes(HttpHeaders.AUTHORIZATION,new SecurityScheme().scheme("bearer").bearerFormat("JWT").type(SecurityScheme.Type.HTTP));
        }
        //csrf
        if(header.isCsrf()){
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

