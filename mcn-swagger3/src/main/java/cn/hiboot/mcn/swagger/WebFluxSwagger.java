package cn.hiboot.mcn.swagger;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.webflux.ui.SwaggerResourceResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2019/6/29 11:34
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class WebFluxSwagger {

    @Bean
    SwaggerResourceResolver swaggerResourceResolver(SwaggerUiConfigProperties swaggerUiConfigProperties) {
        return new SwaggerResourceResolver(swaggerUiConfigProperties){
            @Override
            protected String findWebJarResourcePath(String path) {
                if(path.startsWith("swagger-ui/")){
                    return super.findWebJarResourcePath(path);
                }
                return path;
            }
        };
    }

}
