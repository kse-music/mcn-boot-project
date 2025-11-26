package cn.hiboot.mcn.cloud.feign;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorJacksonConfig;
import feign.Feign;
import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.optionals.OptionalDecoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Feign扩展配置
 *
 * @author DingHao
 * @since 2021/9/21 13:37
 */
@AutoConfiguration(before = FeignAutoConfiguration.class)
@ConditionalOnClass(Feign.class)
@Import(FeignInterceptorConfiguration.class)
public class FeignExtensionAutoConfiguration {


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ErrorDecoder.class)
    @ConditionalOnMissingBean(ErrorDecoder.class)
    protected static class FeignErrorDecoder implements ErrorDecoder {

        @Override
        public Exception decode(String methodKey, Response response) {
            return FeignException.errorStatus(methodKey, response);
        }

    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "param.processor", name = "enabled", havingValue = "true")
    public Decoder feignDecoder(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
        return new OptionalDecoder(new ResponseEntityDecoder(new FeignClientResponseInterceptor(messageConverters)));
    }

    static class FeignClientResponseInterceptor extends SpringDecoder {

        public FeignClientResponseInterceptor(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
            super(messageConverters);
        }

        @Override
        public Object decode(final Response response, Type type) throws IOException, FeignException {
            try {
                return super.decode(response, type);
            } finally {
                NameValueProcessorJacksonConfig.removeFeignRequest();
            }
        }

    }

}
