package cn.hiboot.mcn.cloud.feign;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorJacksonConfig;
import cn.hiboot.mcn.autoconfigure.web.filter.integrity.DataIntegrityUtils;
import cn.hiboot.mcn.cloud.security.SessionHolder;
import cn.hiboot.mcn.core.tuples.Triplet;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * ParamProcessorInterceptor
 *
 * @author DingHao
 * @since 2022/7/20 21:54
 */
@Configuration(proxyBeanMethods = false)
public class FeignInterceptorConfiguration  {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "data.integrity.interceptor",name = "enabled",havingValue = "true")
    private static class DataIntegrityFeignInterceptor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            Map<String, Object> params = new HashMap<>();
            template.queries().forEach((k,v) -> {
                for (String value : v) {
                    params.put(k,value);
                    break;
                }
            });
            String data = null;
            Collection<String> ct = template.headers().get(HttpHeaders.CONTENT_TYPE);
            if(ct != null && MediaType.APPLICATION_JSON_VALUE.equals(new ArrayList<>(ct).get(0))){
                data = new String(template.body(), StandardCharsets.UTF_8);
            }
            Triplet<String, String, String> triplet = DataIntegrityUtils.signature(params, data);
            template.header("TSM", triplet.getValue0());
            template.header("nonceStr", triplet.getValue1());
            template.header("signature", triplet.getValue2());
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "param.processor",name = "enabled",havingValue = "true")
    private static class ParamProcessorInterceptor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            NameValueProcessorJacksonConfig.setFeignRequest();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({DefaultAuthenticationEventPublisher.class, JwtAuthenticationToken.class})
    private static class FeignRequestInterceptor implements RequestInterceptor, Ordered {

        @Override
        public void apply(RequestTemplate requestTemplate) {
            String authorization = SessionHolder.getBearerToken();
            if (authorization != null) {
                requestTemplate.header(HttpHeaders.AUTHORIZATION, authorization);
            }
        }

        @Override
        public int getOrder() {
            return 0;
        }

    }

}
