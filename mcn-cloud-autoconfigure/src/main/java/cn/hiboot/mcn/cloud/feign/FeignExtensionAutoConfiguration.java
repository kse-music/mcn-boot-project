package cn.hiboot.mcn.cloud.feign;

import cn.hiboot.mcn.cloud.security.SessionHolder;
import feign.*;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.concurrent.TimeUnit;

/**
 * 提供全局fallback机制
 *
 * @author DingHao
 * @since 2021/9/21 13:37
 */
@AutoConfiguration
@ConditionalOnClass(Feign.class)
@EnableConfigurationProperties(FeignExtensionProperties.class)
public class FeignExtensionAutoConfiguration {

    private final FeignExtensionProperties properties;

    public FeignExtensionAutoConfiguration(FeignExtensionProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return properties.getLevel();
    }

    @Bean
    public Request.Options options(){
        return new Request.Options(properties.getConnectTimeout().getSeconds(), TimeUnit.SECONDS, properties.getReadTimeout().getSeconds(), TimeUnit.SECONDS, properties.isFollowRedirects());
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({DefaultAuthenticationEventPublisher.class,JwtAuthenticationToken.class})
    protected static class FeignRequestInterceptor implements RequestInterceptor {

        private static final String TOKEN_TYPE = "Bearer ";
        private static final String AUTHORIZATION = "Authorization";

        @Override
        public void apply(RequestTemplate requestTemplate) {
            String authorization = SessionHolder.getToken();
            if (authorization != null) {
                requestTemplate.header(AUTHORIZATION, TOKEN_TYPE.concat(authorization));
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    protected static class FeignErrorDecoder implements ErrorDecoder {

        @Override
        public Exception decode(String methodKey, Response response) {
            return FeignException.errorStatus(methodKey, response);
        }

    }

}
