package cn.hiboot.mcn.cloud.feign;

import cn.hiboot.mcn.autoconfigure.web.filter.common.ValueProcessorJacksonConfig;
import cn.hiboot.mcn.cloud.security.SessionHolder;
import feign.*;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.optionals.OptionalDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 提供全局fallback机制
 *
 * @author DingHao
 * @since 2021/9/21 13:37
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Feign.class)
@Import(FeignInterceptorConfiguration.class)
public class FeignExtensionAutoConfiguration {


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
    @ConditionalOnClass(ErrorDecoder.class)
    protected static class FeignErrorDecoder implements ErrorDecoder {

        @Override
        public Exception decode(String methodKey, Response response) {
            return FeignException.errorStatus(methodKey, response);
        }

    }


    @Bean
    @ConditionalOnMissingBean
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new OptionalDecoder(new ResponseEntityDecoder(new FeignClientResponseInterceptor(messageConverters)));
    }

    static class FeignClientResponseInterceptor extends SpringDecoder {

        public FeignClientResponseInterceptor(ObjectFactory<HttpMessageConverters> messageConverters) {
            super(messageConverters);
        }

        @Override
        public Object decode(final Response response, Type type) throws IOException, FeignException {
            try{
                return super.decode(response,type);
            } finally {
                ValueProcessorJacksonConfig.removeFeignRequest();
            }
        }

    }

}
